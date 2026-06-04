#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BENCHMARK_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${BENCHMARK_DIR}/docker-compose.yml"

DURATION="${DURATION:-60}"
JOBS="${JOBS:-4}"
CLIENTS="${CLIENTS:-10}"
ISOLATION_LEVEL="${ISOLATION_LEVEL:-READ COMMITTED}"
RESET_DB="${RESET_DB:-true}"

RESULT_DIR="${BENCHMARK_DIR}/results"
safe_isolation="$(printf "%s" "${ISOLATION_LEVEL}" | tr '[:upper:] ' '[:lower:]_')"
RESULT_FILE="${RESULT_FILE:-${RESULT_DIR}/${safe_isolation}_clients_${CLIENTS}.txt}"

mkdir -p "${RESULT_DIR}"

cd "${BENCHMARK_DIR}"

start_database() {
  echo "Starting benchmark database..."
  docker compose -f "${COMPOSE_FILE}" up -d

  echo "Waiting for PostgreSQL to become ready..."
  ready="false"
  for _ in $(seq 1 60); do
    if docker compose -f "${COMPOSE_FILE}" exec -T postgres pg_isready -U kuneun -d benchmark >/dev/null 2>&1; then
      ready="true"
      break
    fi
    sleep 1
  done

  if [ "${ready}" != "true" ]; then
    echo "PostgreSQL did not become ready in time." >&2
    exit 1
  fi
}

if [ "${RESET_DB}" = "true" ]; then
  echo "Resetting benchmark database..."
  docker compose -f "${COMPOSE_FILE}" down -v >/dev/null 2>&1 || true
fi

start_database

echo "Running ${ISOLATION_LEVEL}, clients=${CLIENTS}, jobs=${JOBS}, duration=${DURATION}s"

raw_output="$(mktemp)"
set +e
docker compose -f "${COMPOSE_FILE}" exec -T -e PGPASSWORD=1234 postgres pgbench \
  -h localhost \
  -p 5432 \
  -U kuneun \
  -d benchmark \
  -f /workload/blog_mixed_workload.sql \
  -D "isolation_level=${ISOLATION_LEVEL}" \
  -n \
  -c "${CLIENTS}" \
  -j "${JOBS}" \
  -T "${DURATION}" \
  -r \
  --failures-detailed \
  > "${raw_output}" 2>&1
exit_status="$?"
set -e

tps="$(awk -F '=' '/^tps =/ { gsub(/^[ \t]+|[ \t]+$/, "", $2); split($2, value, " "); print value[1]; exit }' "${raw_output}")"
latency="$(awk -F '=' '/^latency average =/ { gsub(/^[ \t]+|[ \t]+$/, "", $2); split($2, value, " "); print value[1]; exit }' "${raw_output}")"
failed="$(awk -F ':' '/^number of failed transactions:/ { gsub(/^[ \t]+|[ \t]+$/, "", $2); split($2, value, " "); print value[1]; exit }' "${raw_output}")"
transactions="$(awk -F ':' '/^number of transactions actually processed:/ { gsub(/^[ \t]+|[ \t]+$/, "", $2); split($2, value, " "); print value[1]; exit }' "${raw_output}")"

{
  printf "isolation: %s\n" "${ISOLATION_LEVEL}"
  printf "clients: %s\n" "${CLIENTS}"
  printf "jobs: %s\n" "${JOBS}"
  printf "duration_sec: %s\n" "${DURATION}"
  printf "database: benchmark\n"
  printf "workload: /workload/blog_mixed_workload.sql\n"
  printf "reset_db: %s\n" "${RESET_DB}"
  printf "exit_status: %s\n" "${exit_status}"
  printf "transactions: %s\n" "${transactions:-N/A}"
  printf "failed_transactions: %s\n" "${failed:-0}"
  printf "latency_average_ms: %s\n" "${latency:-N/A}"
  printf "tps: %s\n" "${tps:-N/A}"
} > "${RESULT_FILE}"

rm -f "${raw_output}"

echo "Benchmark finished."
echo "Result: ${RESULT_FILE}"

exit "${exit_status}"

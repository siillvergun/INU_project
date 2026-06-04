# pgbench Benchmark

이 폴더는 기존 애플리케이션용 `docker-compose.yml`과 분리된 PostgreSQL 벤치마크 환경입니다.

## 구성

- `docker-compose.yml`: 벤치마크 전용 PostgreSQL 컨테이너
- `sql/01_schema.sql`: 사용자, 게시글, 댓글, 좋아요 릴레이션 생성
- `sql/02_seed.sql`: 벤치마크용 초기 데이터 생성
- `workload/blog_mixed_workload.sql`: pgbench가 반복 실행할 블로그 혼합 workload
- `scripts/run_benchmark.sh`: isolation level과 동시 클라이언트 수를 지정해 단일 벤치마크 실행

## 실행

```bash
cd benchmark
docker compose up -d
```

DB가 준비됐는지 확인합니다.

```bash
docker compose ps
```

벤치마크를 실행합니다. 한 번 실행할 때 하나의 조건만 측정합니다.

기본값은 `READ COMMITTED`, 동시 클라이언트 `10`, 실행 시간 `60`초입니다.

```bash
scripts/run_benchmark.sh
```

다른 조건은 환경변수로 지정합니다.

```bash
ISOLATION_LEVEL="READ COMMITTED" CLIENTS=50 scripts/run_benchmark.sh
ISOLATION_LEVEL="READ COMMITTED" CLIENTS=100 scripts/run_benchmark.sh
ISOLATION_LEVEL="SERIALIZABLE" CLIENTS=10 scripts/run_benchmark.sh
ISOLATION_LEVEL="SERIALIZABLE" CLIENTS=50 scripts/run_benchmark.sh
ISOLATION_LEVEL="SERIALIZABLE" CLIENTS=100 scripts/run_benchmark.sh
```

강의 방식대로 비교하려면 다음 6개를 각각 실행합니다.

- `READ COMMITTED`, `-c 10`
- `READ COMMITTED`, `-c 50`
- `READ COMMITTED`, `-c 100`
- `SERIALIZABLE`, `-c 10`
- `SERIALIZABLE`, `-c 50`
- `SERIALIZABLE`, `-c 100`

결과는 `results/` 아래에 조건별 텍스트 파일로 저장됩니다.

예:

```text
results/read_committed_clients_10.txt
results/serializable_clients_100.txt
```

결과 파일은 다음 형식으로 시작합니다.

```text
isolation: READ COMMITTED
clients: 10
jobs: 4
duration_sec: 60
database: benchmark
workload: /workload/blog_mixed_workload.sql
reset_db: true
exit_status: 0
transactions: 1234
failed_transactions: 0
latency_average_ms: 12.345
tps: 456.789
```

기본적으로 실행 전에 벤치마크 DB 볼륨을 초기화합니다. workload가 댓글을 계속 추가하므로, 각 조건을 같은 초기 데이터에서 비교하기 위한 설정입니다.

기본 실행 시간은 `60`초입니다.

초기화 없이 같은 DB에서 실행하려면 다음처럼 실행합니다.

```bash
RESET_DB=false scripts/run_benchmark.sh
```

실행 시간을 바꿔야 할 때만 환경변수로 지정합니다.

```bash
DURATION=300 scripts/run_benchmark.sh
```

수동으로 한 번만 실행하려면 아래 명령을 사용합니다.

Docker 컨테이너 안의 `pgbench`를 사용할 경우:

```bash
docker compose exec postgres pgbench \
  -h localhost \
  -p 5432 \
  -U kuneun \
  -d benchmark \
  -f /workload/blog_mixed_workload.sql \
  -D "isolation_level=READ COMMITTED" \
  -n \
  -c 10 \
  -j 4 \
  -T 60
```

비밀번호를 물어보면 `1234`를 입력합니다. 비밀번호 입력 없이 실행하려면 다음처럼 실행합니다.

```bash
docker compose exec -e PGPASSWORD=1234 postgres pgbench \
  -h localhost \
  -p 5432 \
  -U kuneun \
  -d benchmark \
  -f /workload/blog_mixed_workload.sql \
  -D "isolation_level=READ COMMITTED" \
  -n \
  -c 10 \
  -j 4 \
  -T 60
```

로컬 또는 EC2 서버에 `pgbench`가 설치되어 있다면 호스트 포트 `5433`으로도 실행할 수 있습니다.

```bash
PGPASSWORD=1234 pgbench \
  -h localhost \
  -p 5433 \
  -U kuneun \
  -d benchmark \
  -f workload/blog_mixed_workload.sql \
  -D "isolation_level=READ COMMITTED" \
  -n \
  -c 10 \
  -j 4 \
  -T 60
```

## 옵션 의미

- `-h localhost`: 접속할 PostgreSQL 서버 주소
- `-p 5432`: PostgreSQL 포트. 컨테이너 밖에서 실행하면 `5433` 사용
- `-U kuneun`: DB 사용자
- `-d benchmark`: 대상 데이터베이스
- `-f /workload/blog_mixed_workload.sql`: 반복 실행할 SQL 파일
- `-D "isolation_level=READ COMMITTED"`: workload의 트랜잭션 isolation level 지정
- `-n`: pgbench 기본 vacuum 작업 생략
- `-c 10`: 동시 클라이언트 10개
- `-j 4`: 워커 스레드 4개
- `-T 60`: 60초 동안 실행

## 결과 해석

결과에서 주로 볼 값은 다음 두 가지입니다.

- `latency average`: 트랜잭션 하나가 끝나는 데 걸린 평균 시간
- `tps`: 초당 처리한 트랜잭션 수

현재 workload의 트랜잭션 하나는 게시글 목록 조회, 게시글 상세 조회, 댓글 조회, 댓글 작성, 게시글 좋아요 반영을 하나의 작업으로 묶은 것입니다.

PostgreSQL의 기본 isolation level은 `READ COMMITTED`입니다. 이 벤치마크는 기본값에만 의존하지 않고 workload의 `BEGIN ISOLATION LEVEL ...`에서 `READ COMMITTED` 또는 `SERIALIZABLE`을 명시합니다.

## 초기화

벤치마크 데이터를 처음 상태로 다시 만들려면 볼륨까지 삭제한 뒤 다시 실행합니다.

```bash
docker compose down -v
docker compose up -d
```

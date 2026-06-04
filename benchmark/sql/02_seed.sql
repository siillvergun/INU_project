INSERT INTO users (email, nickname, password, created_at, updated_at)
SELECT
    'user' || gs || '@example.com',
    'user' || gs,
    '$2a$10$benchmark-password-hash-placeholder',
    now() - (gs || ' minutes')::interval,
    now() - (gs || ' minutes')::interval
FROM generate_series(1, 1000) AS gs;

INSERT INTO posts (user_id, title, content, like_count, created_at, updated_at)
SELECT
    ((gs - 1) % 1000) + 1,
    'Benchmark post ' || gs,
    'Benchmark content for post ' || gs || '. This row is used for pgbench workload tests.',
    0,
    now() - (gs || ' seconds')::interval,
    now() - (gs || ' seconds')::interval
FROM generate_series(1, 5000) AS gs;

INSERT INTO comments (post_id, user_id, content, like_count, created_at, updated_at)
SELECT
    ((gs - 1) % 5000) + 1,
    ((gs * 7 - 1) % 1000) + 1,
    'Benchmark comment ' || gs,
    0,
    now() - (gs || ' seconds')::interval,
    now() - (gs || ' seconds')::interval
FROM generate_series(1, 20000) AS gs;

INSERT INTO post_likes (user_id, post_id)
SELECT DISTINCT
    ((gs * 11 - 1) % 1000) + 1,
    ((gs * 13 - 1) % 5000) + 1
FROM generate_series(1, 10000) AS gs
ON CONFLICT DO NOTHING;

UPDATE posts p
SET like_count = like_counts.cnt,
    updated_at = now()
FROM (
    SELECT post_id, count(*) AS cnt
    FROM post_likes
    GROUP BY post_id
) AS like_counts
WHERE p.post_id = like_counts.post_id;

ANALYZE;

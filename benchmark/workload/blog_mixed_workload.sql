\set user_id random(1, 1000)
\set post_id random(1, 5000)

BEGIN ISOLATION LEVEL :isolation_level;

SELECT p.post_id, p.title, p.like_count, p.created_at, u.nickname
FROM posts p
JOIN users u ON u.user_id = p.user_id
ORDER BY p.created_at DESC
LIMIT 20;

SELECT p.post_id, p.title, p.content, p.like_count, u.nickname
FROM posts p
JOIN users u ON u.user_id = p.user_id
WHERE p.post_id = :post_id;

SELECT c.comment_id, c.content, c.like_count, c.created_at, u.nickname
FROM comments c
JOIN users u ON u.user_id = c.user_id
WHERE c.post_id = :post_id
ORDER BY c.created_at DESC
LIMIT 10;

INSERT INTO comments (post_id, user_id, content, like_count, created_at, updated_at)
VALUES (
    :post_id,
    :user_id,
    concat('pgbench comment from user ', :user_id, ' on post ', :post_id),
    0,
    now(),
    now()
);

INSERT INTO post_likes (user_id, post_id)
VALUES (:user_id, :post_id)
ON CONFLICT DO NOTHING;

UPDATE posts
SET like_count = (
        SELECT count(*)
        FROM post_likes
        WHERE post_likes.post_id = posts.post_id
    ),
    updated_at = now()
WHERE post_id = :post_id;

COMMIT;

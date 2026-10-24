ALTER TABLE cms_article
    ADD COLUMN like_count BIGINT NOT NULL DEFAULT 0 AFTER view_count,
    ADD KEY idx_cms_article_view_rank (status, deleted, view_count),
    ADD KEY idx_cms_article_like_rank (status, deleted, like_count);

CREATE TABLE cms_article_like (
    id BIGINT NOT NULL AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    ip_hash VARCHAR(64) NOT NULL,
    like_day DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cms_article_like_day (article_id, ip_hash, like_day),
    KEY idx_cms_article_like_ip_day (ip_hash, like_day)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞记录';

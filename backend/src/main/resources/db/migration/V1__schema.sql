CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    avatar_url VARCHAR(500) NULL,
    email VARCHAR(120) NULL,
    phone VARCHAR(30) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    last_login_at DATETIME NULL,
    last_login_ip VARCHAR(64) NULL,
    remark VARCHAR(500) NULL,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台用户';

CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(500) NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    built_in TINYINT NOT NULL DEFAULT 0,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code),
    KEY idx_sys_role_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台角色';

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    KEY idx_sys_user_role_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联';

CREATE TABLE sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(16) NOT NULL,
    path VARCHAR(200) NULL,
    component_key VARCHAR(100) NULL,
    permission_code VARCHAR(100) NULL,
    icon VARCHAR(100) NULL,
    sort INT NOT NULL DEFAULT 0,
    visible TINYINT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_menu_permission (permission_code),
    KEY idx_sys_menu_parent_sort (parent_id, sort),
    KEY idx_sys_menu_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台菜单与权限点';

CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, menu_id),
    KEY idx_sys_role_menu_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联';

CREATE TABLE sys_dict_type (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dict_code VARCHAR(80) NOT NULL,
    name VARCHAR(80) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(500) NULL,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_dict_type_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型';

CREATE TABLE sys_dict_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dict_type_id BIGINT NOT NULL,
    label VARCHAR(100) NOT NULL,
    value VARCHAR(100) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(500) NULL,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_dict_item_value (dict_type_id, value),
    KEY idx_sys_dict_item_sort (dict_type_id, status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典项';

CREATE TABLE cms_category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(80) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    cover_url VARCHAR(500) NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    seo_title VARCHAR(200) NULL,
    seo_keywords VARCHAR(300) NULL,
    seo_description VARCHAR(500) NULL,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cms_category_slug (slug),
    KEY idx_cms_category_parent_sort (parent_id, sort),
    KEY idx_cms_category_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类';

CREATE TABLE cms_tag (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description VARCHAR(300) NULL,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cms_tag_name (name),
    UNIQUE KEY uk_cms_tag_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签';

CREATE TABLE cms_article (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    summary VARCHAR(500) NULL,
    cover_url VARCHAR(500) NULL,
    content LONGTEXT NOT NULL,
    content_html LONGTEXT NOT NULL,
    content_text LONGTEXT NULL,
    content_format VARCHAR(20) NOT NULL DEFAULT 'HTML',
    category_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    is_top TINYINT NOT NULL DEFAULT 0,
    is_recommended TINYINT NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    published_at DATETIME NULL,
    seo_title VARCHAR(200) NULL,
    seo_keywords VARCHAR(300) NULL,
    seo_description VARCHAR(500) NULL,
    version INT NOT NULL DEFAULT 0,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cms_article_slug (slug),
    KEY idx_cms_article_status_time (status, deleted, published_at),
    KEY idx_cms_article_category (category_id, status, deleted, published_at),
    KEY idx_cms_article_author (author_id),
    KEY idx_cms_article_home (status, deleted, is_top, is_recommended, published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章';

CREATE TABLE cms_article_tag (
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    KEY idx_cms_article_tag_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联';

CREATE TABLE cms_page (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    content LONGTEXT NOT NULL,
    content_html LONGTEXT NOT NULL,
    content_text LONGTEXT NULL,
    content_format VARCHAR(20) NOT NULL DEFAULT 'HTML',
    template VARCHAR(100) NOT NULL DEFAULT 'page/detail',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    sort INT NOT NULL DEFAULT 0,
    seo_title VARCHAR(200) NULL,
    seo_keywords VARCHAR(300) NULL,
    seo_description VARCHAR(500) NULL,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cms_page_slug (slug),
    KEY idx_cms_page_status_sort (status, deleted, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单页面';

CREATE TABLE cms_nav_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    menu_type VARCHAR(20) NOT NULL,
    target_id BIGINT NULL,
    url VARCHAR(500) NULL,
    icon VARCHAR(100) NULL,
    sort INT NOT NULL DEFAULT 0,
    visible TINYINT NOT NULL DEFAULT 1,
    open_type VARCHAR(10) NOT NULL DEFAULT '_self',
    status TINYINT NOT NULL DEFAULT 1,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_cms_nav_parent_sort (parent_id, sort),
    KEY idx_cms_nav_visible (status, visible, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='前台导航';

CREATE TABLE cms_link (
    id BIGINT NOT NULL AUTO_INCREMENT,
    link_type VARCHAR(20) NOT NULL,
    group_name VARCHAR(50) NOT NULL DEFAULT '默认分组',
    name VARCHAR(100) NOT NULL,
    url VARCHAR(500) NOT NULL,
    logo_url VARCHAR(500) NULL,
    description VARCHAR(300) NULL,
    sort INT NOT NULL DEFAULT 0,
    nofollow TINYINT NOT NULL DEFAULT 0,
    open_type VARCHAR(10) NOT NULL DEFAULT '_blank',
    status TINYINT NOT NULL DEFAULT 1,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_cms_link_type_sort (link_type, status, deleted, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='友情链接与常用链接';

CREATE TABLE cms_album (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    cover_url VARCHAR(500) NULL,
    description TEXT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cms_album_slug (slug),
    KEY idx_cms_album_status_sort (status, deleted, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='相册';

CREATE TABLE cms_media (
    id BIGINT NOT NULL AUTO_INCREMENT,
    original_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    url VARCHAR(500) NOT NULL,
    thumb_url VARCHAR(500) NULL,
    mime_type VARCHAR(100) NOT NULL,
    extension VARCHAR(20) NOT NULL,
    size_bytes BIGINT NOT NULL,
    width INT NULL,
    height INT NULL,
    sha256 CHAR(64) NULL,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_cms_media_created (created_at),
    KEY idx_cms_media_sha256 (sha256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒体元数据';

CREATE TABLE cms_album_photo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    album_id BIGINT NOT NULL,
    media_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    thumb_url VARCHAR(500) NULL,
    caption VARCHAR(300) NULL,
    sort INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_cms_album_photo_sort (album_id, deleted, sort),
    KEY idx_cms_album_photo_media (media_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='相册照片';

CREATE TABLE cms_site_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL,
    config_value LONGTEXT NULL,
    value_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    group_code VARCHAR(50) NOT NULL,
    label VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    is_public TINYINT NOT NULL DEFAULT 1,
    sort INT NOT NULL DEFAULT 0,
    updated_by BIGINT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cms_site_config_key (config_key),
    KEY idx_cms_site_config_group (group_code, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点配置';

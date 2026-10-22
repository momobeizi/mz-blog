INSERT INTO sys_user (id, username, password, nickname, avatar_url, email, status, remark)
VALUES (1, 'admin', '$2b$12$r2v6ULGSO.WYe9xdRFAjmuQNiH8qdguCJs5EHXQ.stpiSWizSceZ.', '站长', '/assets/avatar.svg', 'admin@example.com', 1, '初始超级管理员');

INSERT INTO sys_role (id, code, name, description, sort, status, built_in)
VALUES
    (1, 'SUPER_ADMIN', '超级管理员', '拥有全部后台权限', 1, 1, 1),
    (2, 'EDITOR', '内容编辑', '管理文章、页面、相册和站点内容', 2, 1, 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

INSERT INTO sys_menu (id, parent_id, name, type, path, component_key, permission_code, icon, sort, visible, status) VALUES
    (1, 0, '仪表盘', 'MENU', '/dashboard', 'dashboard', 'dashboard:view', 'DashboardOutlined', 10, 1, 1),
    (10, 0, '内容管理', 'DIR', '/content', NULL, NULL, 'FileTextOutlined', 20, 1, 1),
    (11, 10, '文章管理', 'MENU', '/content/articles', 'content/article/list', 'content:article:list', 'ReadOutlined', 10, 1, 1),
    (111, 11, '新建文章', 'BUTTON', NULL, NULL, 'content:article:create', NULL, 1, 1, 1),
    (112, 11, '编辑文章', 'BUTTON', NULL, NULL, 'content:article:update', NULL, 2, 1, 1),
    (113, 11, '删除文章', 'BUTTON', NULL, NULL, 'content:article:delete', NULL, 3, 1, 1),
    (114, 11, '发布文章', 'BUTTON', NULL, NULL, 'content:article:publish', NULL, 4, 1, 1),
    (12, 10, '分类管理', 'MENU', '/content/categories', 'content/category/list', 'content:category:list', 'FolderOpenOutlined', 20, 1, 1),
    (121, 12, '新建分类', 'BUTTON', NULL, NULL, 'content:category:create', NULL, 1, 1, 1),
    (122, 12, '编辑分类', 'BUTTON', NULL, NULL, 'content:category:update', NULL, 2, 1, 1),
    (123, 12, '删除分类', 'BUTTON', NULL, NULL, 'content:category:delete', NULL, 3, 1, 1),
    (13, 10, '标签管理', 'MENU', '/content/tags', 'content/tag/list', 'content:tag:list', 'TagsOutlined', 30, 1, 1),
    (131, 13, '新建标签', 'BUTTON', NULL, NULL, 'content:tag:create', NULL, 1, 1, 1),
    (132, 13, '编辑标签', 'BUTTON', NULL, NULL, 'content:tag:update', NULL, 2, 1, 1),
    (133, 13, '删除标签', 'BUTTON', NULL, NULL, 'content:tag:delete', NULL, 3, 1, 1),
    (14, 10, '单页面', 'MENU', '/content/pages', 'content/page/list', 'content:page:list', 'FileDoneOutlined', 40, 1, 1),
    (141, 14, '新建单页', 'BUTTON', NULL, NULL, 'content:page:create', NULL, 1, 1, 1),
    (142, 14, '编辑单页', 'BUTTON', NULL, NULL, 'content:page:update', NULL, 2, 1, 1),
    (143, 14, '删除单页', 'BUTTON', NULL, NULL, 'content:page:delete', NULL, 3, 1, 1),
    (15, 10, '相册管理', 'MENU', '/content/albums', 'content/album/list', 'content:album:list', 'PictureOutlined', 50, 1, 1),
    (151, 15, '新建相册', 'BUTTON', NULL, NULL, 'content:album:create', NULL, 1, 1, 1),
    (152, 15, '编辑相册', 'BUTTON', NULL, NULL, 'content:album:update', NULL, 2, 1, 1),
    (153, 15, '删除相册', 'BUTTON', NULL, NULL, 'content:album:delete', NULL, 3, 1, 1),
    (16, 10, '媒体库', 'MENU', '/content/media', 'content/media/list', 'content:media:list', 'PictureOutlined', 60, 1, 1),
    (161, 16, '上传媒体', 'BUTTON', NULL, NULL, 'content:media:upload', NULL, 1, 1, 1),
    (162, 16, '删除媒体', 'BUTTON', NULL, NULL, 'content:media:delete', NULL, 2, 1, 1),
    (20, 0, '导航管理', 'DIR', '/navigation', NULL, NULL, 'CompassOutlined', 30, 1, 1),
    (21, 20, '前台菜单', 'MENU', '/navigation/menus', 'nav/menu/list', 'content:nav:list', 'MenuOutlined', 10, 1, 1),
    (211, 21, '新建菜单', 'BUTTON', NULL, NULL, 'content:nav:create', NULL, 1, 1, 1),
    (212, 21, '编辑菜单', 'BUTTON', NULL, NULL, 'content:nav:update', NULL, 2, 1, 1),
    (213, 21, '删除菜单', 'BUTTON', NULL, NULL, 'content:nav:delete', NULL, 3, 1, 1),
    (22, 20, '链接管理', 'MENU', '/navigation/links', 'link/list', 'content:link:list', 'LinkOutlined', 20, 1, 1),
    (221, 22, '新建链接', 'BUTTON', NULL, NULL, 'content:link:create', NULL, 1, 1, 1),
    (222, 22, '编辑链接', 'BUTTON', NULL, NULL, 'content:link:update', NULL, 2, 1, 1),
    (223, 22, '删除链接', 'BUTTON', NULL, NULL, 'content:link:delete', NULL, 3, 1, 1),
    (30, 0, '网站配置', 'MENU', '/settings/site', 'settings/site', 'system:config:view', 'SettingOutlined', 40, 1, 1),
    (31, 30, '保存配置', 'BUTTON', NULL, NULL, 'system:config:update', NULL, 1, 1, 1),
    (40, 0, '系统管理', 'DIR', '/system', NULL, NULL, 'SafetyCertificateOutlined', 50, 1, 1),
    (41, 40, '用户管理', 'MENU', '/system/users', 'system/user/list', 'system:user:list', 'TeamOutlined', 10, 1, 1),
    (411, 41, '新建用户', 'BUTTON', NULL, NULL, 'system:user:create', NULL, 1, 1, 1),
    (412, 41, '编辑用户', 'BUTTON', NULL, NULL, 'system:user:update', NULL, 2, 1, 1),
    (413, 41, '删除用户', 'BUTTON', NULL, NULL, 'system:user:delete', NULL, 3, 1, 1),
    (414, 41, '重置密码', 'BUTTON', NULL, NULL, 'system:user:reset-password', NULL, 4, 1, 1),
    (415, 41, '分配角色', 'BUTTON', NULL, NULL, 'system:user:assign-role', NULL, 5, 1, 1),
    (42, 40, '角色管理', 'MENU', '/system/roles', 'system/role/list', 'system:role:list', 'IdcardOutlined', 20, 1, 1),
    (421, 42, '新建角色', 'BUTTON', NULL, NULL, 'system:role:create', NULL, 1, 1, 1),
    (422, 42, '编辑角色', 'BUTTON', NULL, NULL, 'system:role:update', NULL, 2, 1, 1),
    (423, 42, '删除角色', 'BUTTON', NULL, NULL, 'system:role:delete', NULL, 3, 1, 1),
    (424, 42, '分配权限', 'BUTTON', NULL, NULL, 'system:role:assign-menu', NULL, 4, 1, 1),
    (43, 40, '菜单管理', 'MENU', '/system/menus', 'system/menu/list', 'system:menu:list', 'ApartmentOutlined', 30, 1, 1),
    (431, 43, '新建菜单', 'BUTTON', NULL, NULL, 'system:menu:create', NULL, 1, 1, 1),
    (432, 43, '编辑菜单', 'BUTTON', NULL, NULL, 'system:menu:update', NULL, 2, 1, 1),
    (433, 43, '删除菜单', 'BUTTON', NULL, NULL, 'system:menu:delete', NULL, 3, 1, 1),
    (44, 40, '字典管理', 'MENU', '/system/dictionaries', 'system/dict/list', 'system:dict:list', 'BookOutlined', 40, 1, 1),
    (441, 44, '新建字典', 'BUTTON', NULL, NULL, 'system:dict:create', NULL, 1, 1, 1),
    (442, 44, '编辑字典', 'BUTTON', NULL, NULL, 'system:dict:update', NULL, 2, 1, 1),
    (443, 44, '删除字典', 'BUTTON', NULL, NULL, 'system:dict:delete', NULL, 3, 1, 1);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, id FROM sys_menu WHERE id IN (10,11,111,112,113,114,12,121,122,123,13,131,132,133,14,141,142,143,15,151,152,153,16,161,162,20,21,211,212,213,22,221,222,223);

INSERT INTO sys_dict_type (id, dict_code, name, status, remark) VALUES
    (1, 'article_status', '文章状态', 1, '文章发布状态'),
    (2, 'content_format', '正文格式', 1, 'HTML 或 Markdown'),
    (3, 'link_type', '链接类型', 1, '友情链接与常用链接'),
    (4, 'nav_type', '导航类型', 1, '前台导航目标类型'),
    (5, 'page_status', '单页状态', 1, '单页发布状态'),
    (6, 'common_status', '通用状态', 1, '启用与停用');

INSERT INTO sys_dict_item (dict_type_id, label, value, sort, status) VALUES
    (1, '草稿', 'DRAFT', 1, 1), (1, '已发布', 'PUBLISHED', 2, 1), (1, '已下线', 'OFFLINE', 3, 1),
    (2, '富文本', 'HTML', 1, 1), (2, 'Markdown', 'MARKDOWN', 2, 1),
    (3, '友情链接', 'FRIEND', 1, 1), (3, '常用链接', 'COMMON', 2, 1),
    (4, '首页', 'HOME', 1, 1), (4, '分类', 'CATEGORY', 2, 1), (4, '单页', 'PAGE', 3, 1), (4, '相册', 'ALBUM', 4, 1), (4, '自定义', 'CUSTOM', 5, 1),
    (5, '草稿', 'DRAFT', 1, 1), (5, '已发布', 'PUBLISHED', 2, 1), (5, '已下线', 'OFFLINE', 3, 1),
    (6, '停用', '0', 1, 1), (6, '启用', '1', 2, 1);

INSERT INTO cms_category (id, parent_id, name, slug, description, sort, status) VALUES
    (1, 0, '生活随笔', 'life', '日常、心情与生活里的小事', 10, 1),
    (2, 0, '技术札记', 'tech', '工程实践、源码与工具', 20, 1),
    (3, 0, '阅读观影', 'reading', '书、电影与长时间的回声', 30, 1),
    (4, 0, '旅行见闻', 'travel', '去过的城市与看过的海', 40, 1);

INSERT INTO cms_tag (id, name, slug, description) VALUES
    (1, 'Spring Boot', 'spring-boot', 'Java 后端实践'),
    (2, '设计', 'design', '界面与体验'),
    (3, '阅读', 'reading', '读书笔记'),
    (4, '旅行', 'travel', '城市与风景'),
    (5, '随笔', 'essay', '随手记录'),
    (6, '效率', 'productivity', '工具与方法');

INSERT INTO cms_article (id, title, slug, summary, cover_url, content, content_html, content_text, content_format, category_id, author_id, status, is_top, is_recommended, view_count, published_at, seo_keywords, seo_description, created_by, updated_by) VALUES
    (1, '把生活写成一座可以回来的花园', 'a-garden-to-return', '记录不是为了证明什么，而是给未来的自己留一盏灯。', '/assets/covers/cover-1.svg',
     '<p>有些日子过得很快，快到只剩下一个模糊的天气。开始写下来之后，才发现那些被忽略的细节，其实一直在发光。</p><h2>从一张照片开始</h2><p>每天留一张照片、一句话，时间久了，生活就有了可以翻阅的形状。</p><h2>让记录变得轻一点</h2><p>不必等到完整，也不必等到有答案。写下一个片段，就已经足够。</p>',
     '<p>有些日子过得很快，快到只剩下一个模糊的天气。开始写下来之后，才发现那些被忽略的细节，其实一直在发光。</p><h2 id="从一张照片开始">从一张照片开始</h2><p>每天留一张照片、一句话，时间久了，生活就有了可以翻阅的形状。</p><h2 id="让记录变得轻一点">让记录变得轻一点</h2><p>不必等到完整，也不必等到有答案。写下一个片段，就已经足够。</p>',
     '有些日子过得很快，快到只剩下一个模糊的天气。开始写下来之后，才发现那些被忽略的细节，其实一直在发光。从一张照片开始。每天留一张照片、一句话，时间久了，生活就有了可以翻阅的形状。让记录变得轻一点。不必等到完整，也不必等到有答案。', 'HTML', 1, 1, 'PUBLISHED', 1, 1, 328, '2026-10-18 20:30:00', '生活,记录,随笔', '记录不是为了证明什么，而是给未来的自己留一盏灯。', 1, 1),
    (2, 'Spring Boot 3 项目里的那些小决定', 'spring-boot-3-decisions', '从会话、权限到内容渲染，记录一次 CMS 工程化的取舍。', '/assets/covers/cover-2.svg',
     '## 为什么选择单体\n\n项目规模不大时，模块化单体比微服务更容易保持一致性。\n\n## 内容渲染\n\n富文本和 Markdown 最终都要进入同一个清洗器。\n\n```java\npublic RenderedContent render(ContentFormat format, String source) {\n    return contentRenderer.render(format, source);\n}\n```',
     '<h2 id="为什么选择单体">为什么选择单体</h2><p>项目规模不大时，模块化单体比微服务更容易保持一致性。</p><h2 id="内容渲染">内容渲染</h2><p>富文本和 Markdown 最终都要进入同一个清洗器。</p><pre><code class="language-java">public RenderedContent render(ContentFormat format, String source) {\n    return contentRenderer.render(format, source);\n}</code></pre>',
     '为什么选择单体。项目规模不大时，模块化单体比微服务更容易保持一致性。内容渲染。富文本和 Markdown 最终都要进入同一个清洗器。', 'MARKDOWN', 2, 1, 'PUBLISHED', 0, 1, 521, '2026-10-12 09:20:00', 'Spring Boot,Java,CMS', '一次 CMS 工程化的取舍记录。', 1, 1),
    (3, '在海边等一场迟到的日落', 'waiting-for-sunset', '风把云吹开的时候，海面像一张缓慢发亮的纸。', '/assets/covers/cover-3.svg',
     '<p>海边的下午很长。人们坐着，走着，或者只是看着远处发呆。</p><h2>光线慢下来</h2><p>日落不是一瞬间发生的，它先落在浪尖，再落在人的肩上。</p><blockquote><p>有些风景不需要被带走，它会在记忆里继续发光。</p></blockquote>',
     '<p>海边的下午很长。人们坐着，走着，或者只是看着远处发呆。</p><h2 id="光线慢下来">光线慢下来</h2><p>日落不是一瞬间发生的，它先落在浪尖，再落在人的肩上。</p><blockquote><p>有些风景不需要被带走，它会在记忆里继续发光。</p></blockquote>',
     '海边的下午很长。人们坐着，走着，或者只是看着远处发呆。光线慢下来。日落不是一瞬间发生的，它先落在浪尖，再落在人的肩上。有些风景不需要被带走，它会在记忆里继续发光。', 'HTML', 4, 1, 'PUBLISHED', 0, 1, 286, '2026-10-03 18:45:00', '旅行,海边,日落', '风把云吹开的时候，海面像一张缓慢发亮的纸。', 1, 1),
    (4, '一本书读完以后，留下什么', 'what-remains-after-reading', '真正留下来的，往往不是结论，而是某个重新看世界的方法。', '/assets/covers/cover-4.svg',
     '<p>读完一本书之后，我习惯隔几天再写笔记。那些还能被想起的句子，才是真正留下来的部分。</p><h2>不急着总结</h2><p>先让它沉一会儿，看看它会不会在生活里再次出现。</p>',
     '<p>读完一本书之后，我习惯隔几天再写笔记。那些还能被想起的句子，才是真正留下来的部分。</p><h2 id="不急着总结">不急着总结</h2><p>先让它沉一会儿，看看它会不会在生活里再次出现。</p>',
     '读完一本书之后，我习惯隔几天再写笔记。那些还能被想起的句子，才是真正留下来的部分。不急着总结。先让它沉一会儿，看看它会不会在生活里再次出现。', 'HTML', 3, 1, 'PUBLISHED', 0, 0, 147, '2026-09-26 21:10:00', '阅读,读书笔记', '真正留下来的，往往不是结论。', 1, 1),
    (5, '给数字花园换一套更轻的字体', 'a-lighter-typeface', '新字体不一定要更华丽，但应该让阅读更安静。', '/assets/covers/cover-5.svg',
     '### 字体的重量\n\n字体是页面的呼吸。过重的标题会让人紧张，过细的正文又容易疲劳。\n\n### 这次的选择\n\n- 标题保留一点书卷气\n- 正文优先屏幕可读性\n- 数字和英文使用更清晰的字形',
     '<h3 id="字体的重量">字体的重量</h3><p>字体是页面的呼吸。过重的标题会让人紧张，过细的正文又容易疲劳。</p><h3 id="这次的选择">这次的选择</h3><ul><li>标题保留一点书卷气</li><li>正文优先屏幕可读性</li><li>数字和英文使用更清晰的字形</li></ul>',
     '字体的重量。字体是页面的呼吸。过重的标题会让人紧张，过细的正文又容易疲劳。这次的选择。标题保留一点书卷气。正文优先屏幕可读性。数字和英文使用更清晰的字形。', 'MARKDOWN', 2, 1, 'PUBLISHED', 0, 0, 203, '2026-09-15 15:00:00', '设计,字体,阅读体验', '让阅读更安静的一次字体调整。', 1, 1),
    (6, '周末去山里，把手机调成静音', 'weekend-in-the-mountains', '山路没有通知栏，只有风穿过树叶的声音。', '/assets/covers/cover-6.svg',
     '<p>周六早上出发，带一本书和一瓶水。山路比想象中安静。</p><h2>没有信号的两个小时</h2><p>一开始总想摸手机，后来就只顾着看脚下的路了。</p><p>回程时天已经暗下来，远处的灯一盏一盏亮起。</p>',
     '<p>周六早上出发，带一本书和一瓶水。山路比想象中安静。</p><h2 id="没有信号的两个小时">没有信号的两个小时</h2><p>一开始总想摸手机，后来就只顾着看脚下的路了。</p><p>回程时天已经暗下来，远处的灯一盏一盏亮起。</p>',
     '周六早上出发，带一本书和一瓶水。山路比想象中安静。没有信号的两个小时。一开始总想摸手机，后来就只顾着看脚下的路了。回程时天已经暗下来，远处的灯一盏一盏亮起。', 'HTML', 4, 1, 'PUBLISHED', 0, 0, 119, '2026-08-30 22:05:00', '旅行,徒步,周末', '山路没有通知栏，只有风穿过树叶的声音。', 1, 1),
    (7, '做一张自己的年度阅读地图', 'reading-map-of-the-year', '把读过的书放在一起看，会看见自己这一年的好奇心。', '/assets/covers/cover-7.svg',
     '<p>年底整理书架的时候，我把今年读过的书按主题重新排了一遍。</p><h2>不是书单，是路径</h2><p>有些书把我带向下一本书，有些书则让我停下来想了很久。</p>',
     '<p>年底整理书架的时候，我把今年读过的书按主题重新排了一遍。</p><h2 id="不是书单是路径">不是书单，是路径</h2><p>有些书把我带向下一本书，有些书则让我停下来想了很久。</p>',
     '年底整理书架的时候，我把今年读过的书按主题重新排了一遍。不是书单，是路径。有些书把我带向下一本书，有些书则让我停下来想了很久。', 'HTML', 3, 1, 'PUBLISHED', 0, 0, 94, '2026-08-11 19:40:00', '阅读,年度总结', '把读过的书放在一起看。', 1, 1),
    (8, '一个适合写长文的编辑器应该是什么样', 'a-good-editor-for-long-writing', '少一点干扰，多一点对文字的尊重。', '/assets/covers/cover-8.svg',
     '编辑器不是功能越多越好。\n\n它应该让人忘记工具本身，只记得正在写的那句话。\n\n## 两个模式\n\n- 富文本：适合图文混排\n- Markdown：适合技术写作\n\n两种模式最终都应该被同一套安全规则保护。',
     '<p>编辑器不是功能越多越好。</p><p>它应该让人忘记工具本身，只记得正在写的那句话。</p><h2 id="两个模式">两个模式</h2><ul><li>富文本：适合图文混排</li><li>Markdown：适合技术写作</li></ul><p>两种模式最终都应该被同一套安全规则保护。</p>',
     '编辑器不是功能越多越好。它应该让人忘记工具本身，只记得正在写的那句话。两个模式。富文本适合图文混排。Markdown适合技术写作。两种模式最终都应该被同一套安全规则保护。', 'MARKDOWN', 2, 1, 'PUBLISHED', 0, 0, 76, '2026-07-28 10:15:00', '写作,编辑器,Markdown', '少一点干扰，多一点对文字的尊重。', 1, 1),
    (9, '草稿：关于秋天的待办清单', 'draft-autumn-list', '等到天气再凉一点，去走一条没有走过的路。', '/assets/covers/cover-4.svg',
     '<p>这是一篇还没有完成的草稿。</p><p>等到天气再凉一点，去走一条没有走过的路。</p>',
     '<p>这是一篇还没有完成的草稿。</p><p>等到天气再凉一点，去走一条没有走过的路。</p>',
     '这是一篇还没有完成的草稿。等到天气再凉一点，去走一条没有走过的路。', 'HTML', 1, 1, 'DRAFT', 0, 0, 0, NULL, '秋天,待办', NULL, 1, 1);

INSERT INTO cms_article_tag (article_id, tag_id) VALUES
    (1,1),(1,5),(2,1),(2,6),(3,4),(3,5),(4,3),(5,2),(6,4),(7,3),(8,1),(8,2);

INSERT INTO cms_page (id, title, slug, content, content_html, content_text, content_format, template, status, sort, created_by, updated_by) VALUES
    (1, '关于本站', 'about', '<p>这里是我的数字花园，用来收藏生活、技术与阅读留下的回声。</p><p>如果你也在记录，很高兴与你相遇。</p>', '<p>这里是我的数字花园，用来收藏生活、技术与阅读留下的回声。</p><p>如果你也在记录，很高兴与你相遇。</p>', '这里是我的数字花园，用来收藏生活、技术与阅读留下的回声。如果你也在记录，很高兴与你相遇。', 'HTML', 'page/detail', 'PUBLISHED', 1, 1, 1),
    (2, '隐私政策', 'privacy', '本站仅收集访问统计所需的匿名信息，不主动出售或共享个人数据。', '<p>本站仅收集访问统计所需的匿名信息，不主动出售或共享个人数据。</p>', '本站仅收集访问统计所需的匿名信息，不主动出售或共享个人数据。', 'MARKDOWN', 'page/detail', 'PUBLISHED', 2, 1, 1),
    (3, '联系我', 'contact', '<p>欢迎交流。<br>邮箱：admin@example.com</p>', '<p>欢迎交流。<br>邮箱：admin@example.com</p>', '欢迎交流。邮箱：admin@example.com', 'HTML', 'page/detail', 'PUBLISHED', 3, 1, 1);

INSERT INTO cms_nav_menu (id, parent_id, name, menu_type, target_id, url, icon, sort, visible, open_type, status, created_by, updated_by) VALUES
    (1, 0, '首页', 'HOME', NULL, NULL, 'home', 10, 1, '_self', 1, 1, 1),
    (2, 0, '文章', 'CUSTOM', NULL, '/articles', 'read', 20, 1, '_self', 1, 1, 1),
    (3, 0, '关于', 'PAGE', 1, NULL, 'info', 30, 1, '_self', 1, 1, 1),
    (4, 0, '相册', 'ALBUM', 1, NULL, 'picture', 40, 1, '_self', 1, 1, 1),
    (5, 0, '友链', 'CUSTOM', NULL, '/links', 'link', 50, 1, '_self', 1, 1, 1),
    (6, 0, '归档', 'CUSTOM', NULL, '/archive', 'archive', 60, 1, '_self', 1, 1, 1);

INSERT INTO cms_link (id, link_type, group_name, name, url, logo_url, description, sort, nofollow, open_type, status, created_by, updated_by) VALUES
    (1, 'FRIEND', '友情链接', 'Spring', 'https://spring.io', NULL, 'Spring 官方社区', 10, 0, '_blank', 1, 1, 1),
    (2, 'FRIEND', '友情链接', 'MDN', 'https://developer.mozilla.org', NULL, 'Web 开发文档', 20, 0, '_blank', 1, 1, 1),
    (3, 'FRIEND', '友情链接', 'GitHub', 'https://github.com', NULL, '代码与开源', 30, 0, '_blank', 1, 1, 1),
    (4, 'COMMON', '常用', '后台管理', '/admin', NULL, '进入内容后台', 10, 0, '_self', 1, 1, 1),
    (5, 'COMMON', '常用', 'RSS 订阅', '/rss.xml', NULL, '订阅最新文章', 20, 0, '_self', 1, 1, 1),
    (6, 'COMMON', '常用', '站点地图', '/sitemap.xml', NULL, '查看全部页面', 30, 0, '_self', 1, 1, 1);

INSERT INTO cms_media (id, original_name, storage_key, url, thumb_url, mime_type, extension, size_bytes, width, height, created_by) VALUES
    (1, 'cover-1.svg', 'covers/cover-1.svg', '/assets/covers/cover-1.svg', '/assets/covers/cover-1.svg', 'image/svg+xml', 'svg', 4200, 1200, 800, 1),
    (2, 'cover-2.svg', 'covers/cover-2.svg', '/assets/covers/cover-2.svg', '/assets/covers/cover-2.svg', 'image/svg+xml', 'svg', 4200, 1200, 800, 1),
    (3, 'cover-3.svg', 'covers/cover-3.svg', '/assets/covers/cover-3.svg', '/assets/covers/cover-3.svg', 'image/svg+xml', 'svg', 4200, 1200, 800, 1),
    (4, 'cover-4.svg', 'covers/cover-4.svg', '/assets/covers/cover-4.svg', '/assets/covers/cover-4.svg', 'image/svg+xml', 'svg', 4200, 1200, 800, 1),
    (5, 'cover-5.svg', 'covers/cover-5.svg', '/assets/covers/cover-5.svg', '/assets/covers/cover-5.svg', 'image/svg+xml', 'svg', 4200, 1200, 800, 1),
    (6, 'cover-6.svg', 'covers/cover-6.svg', '/assets/covers/cover-6.svg', '/assets/covers/cover-6.svg', 'image/svg+xml', 'svg', 4200, 1200, 800, 1),
    (7, 'cover-7.svg', 'covers/cover-7.svg', '/assets/covers/cover-7.svg', '/assets/covers/cover-7.svg', 'image/svg+xml', 'svg', 4200, 1200, 800, 1),
    (8, 'cover-8.svg', 'covers/cover-8.svg', '/assets/covers/cover-8.svg', '/assets/covers/cover-8.svg', 'image/svg+xml', 'svg', 4200, 1200, 800, 1);

INSERT INTO cms_album (id, title, slug, cover_url, description, sort, status, created_by, updated_by) VALUES
    (1, '海边的光', 'seaside-light', '/assets/covers/cover-3.svg', '关于海、风和傍晚的片段。', 10, 1, 1, 1),
    (2, '城市散步', 'city-walk', '/assets/covers/cover-6.svg', '周末走过的一些街道。', 20, 1, 1, 1),
    (3, '日常碎片', 'daily-fragments', '/assets/covers/cover-1.svg', '没有主题的生活记录。', 30, 1, 1, 1);

INSERT INTO cms_album_photo (album_id, media_id, image_url, thumb_url, caption, sort) VALUES
    (1, 3, '/assets/covers/cover-3.svg', '/assets/covers/cover-3.svg', '傍晚的海面', 10),
    (1, 6, '/assets/covers/cover-6.svg', '/assets/covers/cover-6.svg', '风从远处来', 20),
    (2, 5, '/assets/covers/cover-5.svg', '/assets/covers/cover-5.svg', '街角的光', 10),
    (2, 2, '/assets/covers/cover-2.svg', '/assets/covers/cover-2.svg', '夜色里的窗', 20),
    (3, 1, '/assets/covers/cover-1.svg', '/assets/covers/cover-1.svg', '一杯茶', 10),
    (3, 4, '/assets/covers/cover-4.svg', '/assets/covers/cover-4.svg', '读了一半的书', 20);

INSERT INTO cms_site_config (config_key, config_value, value_type, group_code, label, description, is_public, sort) VALUES
    ('site.name', 'mz.blog', 'STRING', 'basic', '站点名称', '显示在导航和页脚', 1, 10),
    ('site.subtitle', '记录、思考与生活', 'STRING', 'basic', '站点副标题', NULL, 1, 20),
    ('site.description', '一座关于生活、技术与阅读的个人数字花园。', 'TEXT', 'basic', '站点描述', NULL, 1, 30),
    ('site.logo', '/assets/avatar.svg', 'STRING', 'basic', '站点 Logo', NULL, 1, 40),
    ('site.favicon', '/assets/favicon.svg', 'STRING', 'basic', 'Favicon', NULL, 1, 50),
    ('site.domain', 'http://localhost:8080', 'STRING', 'basic', '站点域名', '用于 canonical 和 sitemap', 0, 60),
    ('site.hero_title', '相信记录的力量', 'STRING', 'hero', '首屏标题', NULL, 1, 10),
    ('site.hero_subtitle', 'mz.blog · 个人数字花园', 'STRING', 'hero', '首屏副标题', NULL, 1, 20),
    ('site.footer_text', '在喧闹的世界里，留下一块安静的地方。', 'TEXT', 'footer', '页脚文案', NULL, 1, 10),
    ('site.copyright', '© 2026 mz.blog', 'STRING', 'footer', '版权信息', NULL, 1, 20),
    ('site.icp', '', 'STRING', 'footer', '备案号', NULL, 1, 30),
    ('seo.keywords', '个人博客,生活记录,技术札记,阅读', 'STRING', 'seo', '默认关键词', NULL, 1, 10),
    ('seo.description', '一座关于生活、技术与阅读的个人数字花园。', 'TEXT', 'seo', '默认描述', NULL, 1, 20),
    ('seo.og_image', '/assets/covers/cover-1.svg', 'STRING', 'seo', '默认分享图', NULL, 1, 30),
    ('social.github', 'https://github.com', 'STRING', 'social', 'GitHub', NULL, 1, 10),
    ('feature.search', 'true', 'BOOLEAN', 'feature', '搜索开关', NULL, 1, 10),
    ('feature.rss', 'true', 'BOOLEAN', 'feature', 'RSS 开关', NULL, 1, 20),
    ('feature.view_count', 'true', 'BOOLEAN', 'feature', '浏览量开关', NULL, 1, 30),
    ('feature.maintenance', 'false', 'BOOLEAN', 'feature', '维护模式', NULL, 0, 40),
    ('upload.max_size', '10', 'NUMBER', 'upload', '最大上传 MB', NULL, 0, 10);

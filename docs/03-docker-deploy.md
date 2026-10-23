# Docker Compose 部署

## 组成

服务器上已经有 MySQL 和 Nginx，Compose 只部署应用：

- `mz-blog`：Spring Boot 3 + Thymeleaf 前台 + 打包进应用的 React 后台管理系统。
- `uploads`：上传文件持久化卷，容器内路径为 `/data/uploads`。

应用仅绑定宿主机回环地址 `127.0.0.1:${APP_PORT}`，由服务器已有的 Nginx 反向代理对外提供服务。MySQL 也不在 Compose 中启动，应用通过环境变量连接已有数据库。

## 准备 MySQL

建议为本站创建独立数据库和专用账号：

```sql
CREATE DATABASE mz_blog_cms
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'mz_blog'@'%' IDENTIFIED BY '替换为强密码';
GRANT ALL PRIVILEGES ON mz_blog_cms.* TO 'mz_blog'@'%';
FLUSH PRIVILEGES;
```

应用启动时由 Flyway 自动执行迁移并初始化数据，不需要手工导入 SQL。

MySQL 在 Docker 宿主机上时，默认使用 `DB_HOST=host.docker.internal`。Compose 已为 Linux 宿主机添加 `host-gateway` 映射。同时确认：

- MySQL 监听地址允许容器访问，例如监听 `0.0.0.0:3306`。
- MySQL 防火墙或云安全组允许 Docker 网段访问。
- 数据库账号的 Host 允许从 Docker 网络连接，通常使用 `%`，或为具体网段单独授权。

MySQL 在另一台服务器或云数据库时，把 `DB_HOST` 改成实际地址即可。MySQL 与服务在同一个自定义 Docker 网络时，也可以填写 MySQL 容器名，并把 `app` 接入该网络。

## 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env`：

```bash
APP_PORT=8080
SITE_BASE_URL=https://你的域名
COOKIE_SECURE=true

DB_HOST=host.docker.internal
DB_PORT=3306
DB_DATABASE=mz_blog_cms
DB_USERNAME=mz_blog
DB_PASSWORD=替换为数据库密码
```

- `APP_PORT`：应用绑定到宿主机 `127.0.0.1` 的端口，供现有 Nginx 反向代理。
- `SITE_BASE_URL`：站点对外地址，用于 canonical、RSS、Sitemap 和 robots.txt，末尾不要带 `/`。
- 未启用 HTTPS 时使用 `COOKIE_SECURE=false`；启用 HTTPS 后改为 `true`。

## 构建 JAR（开发机）

后台管理系统不是单独运行的前端服务。`admin-web` 构建后的静态文件会写入后端的资源目录，再由 Maven 一起打进 Spring Boot JAR：

```text
admin-web 源码
  -> pnpm build
  -> backend/src/main/resources/static/admin
  -> mvn package
  -> deploy/mz-blog.jar（包含前台模板和后台管理页面）
```

在开发机执行：

```bash
./scripts/build-app.sh
```

脚本会依次完成：

```bash
cd admin-web
pnpm install --frozen-lockfile
pnpm build

cd ../backend
mvn clean package -DskipTests
cp target/mz-blog-*.jar ../deploy/mz-blog.jar
```

也可以手工执行上述命令。构建完成后确认 JAR 存在：

```bash
ls -lh deploy/mz-blog.jar
```

上传到服务器：

```bash
scp deploy/mz-blog.jar 用户@服务器:/项目目录/mz-blog/deploy/mz-blog.jar
```

服务器只需要 Docker，不需要安装 Node.js 或 Maven。

## 部署应用

```bash
docker compose up -d --build
docker compose ps
```

Docker 构建只会把 `deploy/mz-blog.jar` 放入 Java 运行镜像，不会在服务器上重新编译前后端。镜像名为 `mz-blog:latest`，容器名为 `mz-blog`。

查看日志：

```bash
docker compose logs -f mz-blog
```

健康检查：

```bash
curl -f http://127.0.0.1:${APP_PORT:-8080}/actuator/health
```

默认访问地址（经已有 Nginx 转发后）：

- 前台：`https://你的域名/`
- 后台：`https://你的域名/admin`
- 初始账号：`admin`
- 初始密码：`Admin@123`

首次登录后应立即修改后台密码。

## 配置已有 Nginx

仓库中的 `deploy/nginx/default.conf` 是站点配置示例。不要直接覆盖服务器现有的 `default.conf`，建议复制为独立站点文件：

```bash
sudo cp deploy/nginx/default.conf /etc/nginx/conf.d/mz-blog.conf
sudo nginx -t
sudo systemctl reload nginx
```

修改其中的 `server_name` 为实际域名，并确认 `proxy_pass` 与应用端口一致：

```nginx
proxy_pass http://127.0.0.1:8080;
```

如果 `.env` 中使用了其他 `APP_PORT`，同时修改该行。证书和 HTTPS 由服务器已有 Nginx 统一管理。

## 更新发版

开发机重新构建并上传 JAR：

```bash
./scripts/build-app.sh
scp deploy/mz-blog.jar 用户@服务器:/项目目录/mz-blog/deploy/mz-blog.jar
```

服务器更新应用：

```bash
docker compose up -d --build
docker compose ps
```

Flyway 会在应用启动时执行尚未运行的迁移脚本。发版前应备份外部数据库和上传文件。

## 数据库备份与恢复

在服务器安装 MySQL 客户端后执行。先加载 `.env` 中的连接参数：

```bash
set -a
. ./.env
set +a
```

备份：

```bash
mysqldump -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USERNAME" -p"$DB_PASSWORD" \
  --single-transaction --routines --triggers "$DB_DATABASE" \
  > "mz_blog_$(date +%Y%m%d_%H%M%S).sql"
```

恢复：

```bash
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USERNAME" -p"$DB_PASSWORD" "$DB_DATABASE" \
  < backup.sql
```

注意：宿主机执行备份时如果 `DB_HOST=host.docker.internal`，宿主机通常无法解析该名称，应临时改为 `127.0.0.1` 或使用实际数据库地址。

## 上传文件备份

```bash
docker run --rm \
  -v mz-blog_uploads:/data \
  -v "$PWD":/backup \
  alpine tar czf "/backup/uploads_$(date +%Y%m%d).tar.gz" -C /data .
```

恢复上传文件前先停止应用：

```bash
docker compose stop mz-blog
docker run --rm \
  -v mz-blog_uploads:/data \
  -v "$PWD":/backup \
  alpine sh -c 'tar xzf /backup/uploads_YYYYMMDD.tar.gz -C /data'
docker compose start mz-blog
```

## 回滚

保留上一版本的 JAR，例如 `deploy/mz-blog.jar.previous`。回滚时替换 JAR 后重新构建容器：

```bash
cp deploy/mz-blog.jar.previous deploy/mz-blog.jar
docker compose up -d --build
```

如果新版本包含不兼容的数据库迁移，需要同时恢复发版前的数据库备份。生产环境建议只使用向后兼容的迁移脚本，避免直接修改已经执行过的历史迁移。

## 常用命令

```bash
docker compose ps
docker compose logs -f mz-blog
docker compose restart mz-blog
docker compose down
docker compose down -v   # 只删除上传文件卷；外部 MySQL 和 Nginx 不受影响
```

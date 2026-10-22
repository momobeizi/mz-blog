# Docker Compose 部署

## 组成

- `nginx`：对外入口，转发到 Spring Boot。
- `app`：Spring Boot 3 + Thymeleaf + React 后台静态资源。
- `mysql`：MySQL 8，首次启动后由 Flyway 自动建表和初始化数据。
- `mysql-data`：数据库持久化卷。
- `uploads`：上传文件持久化卷，容器内路径为 `/data/uploads`。

## 首次部署

```bash
cp .env.example .env
```

编辑 `.env`，至少修改：

```bash
MYSQL_ROOT_PASSWORD=强密码
MYSQL_PASSWORD=强密码
SITE_BASE_URL=https://你的域名
COOKIE_SECURE=true
```

本地 HTTP 测试时保留 `COOKIE_SECURE=false`，启用 HTTPS 后改为 `true`。

构建并启动：

```bash
docker compose build
docker compose up -d
docker compose ps
```

查看日志：

```bash
docker compose logs -f app
docker compose logs -f mysql
```

健康检查：

```bash
curl -f http://localhost:8080/actuator/health
```

默认访问地址：

- 前台：`http://localhost:8080/`
- 后台：`http://localhost:8080/admin`
- 初始账号：`admin`
- 初始密码：`Admin@123`

首次登录后应立即修改后台密码。

## 更新发版

```bash
git pull
docker compose build app
docker compose up -d app nginx
docker compose ps
```

Flyway 会在应用启动时执行尚未运行的迁移脚本。发版前先备份数据库：

```bash
docker compose exec -T mysql sh -c \
  'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines --triggers "$MYSQL_DATABASE"' \
  > mz_blog_cms_$(date +%Y%m%d_%H%M%S).sql
```

## 数据备份与恢复

备份：

```bash
docker compose exec -T mysql sh -c \
  'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines --triggers "$MYSQL_DATABASE"' \
  > backup.sql
```

恢复：

```bash
cat backup.sql | docker compose exec -T mysql sh -c \
  'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"'
```

上传文件备份：

```bash
docker run --rm \
  -v mz-blog_uploads:/data \
  -v "$PWD":/backup \
  alpine tar czf /backup/uploads_$(date +%Y%m%d).tar.gz -C /data .
```

## 回滚

代码回滚后重新构建应用：

```bash
git checkout <上一个可用版本>
docker compose build app
docker compose up -d app nginx
```

如果新版本包含不兼容的数据库迁移，需要恢复发版前的数据库备份。生产环境建议只使用向后兼容的迁移脚本，避免直接修改已执行的历史迁移。

## HTTPS

生产环境建议在 `nginx` 前增加 TLS 终止，或挂载证书并扩展 `deploy/nginx/default.conf` 的 443 Server 配置。启用 HTTPS 后必须设置：

```bash
SITE_BASE_URL=https://你的域名
COOKIE_SECURE=true
```

## 常用命令

```bash
docker compose ps
docker compose logs -f app
docker compose restart app
docker compose down
docker compose down -v   # 同时删除数据库和上传文件，谨慎使用
```

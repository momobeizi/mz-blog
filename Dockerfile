# syntax=docker/dockerfile:1

FROM node:22-alpine AS admin-build
RUN npm install -g pnpm@10.15.0
WORKDIR /app/admin-web
COPY admin-web/package.json admin-web/pnpm-lock.yaml admin-web/pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile
COPY admin-web/ ./
RUN mkdir -p /app/backend/src/main/resources/static/admin \
    && pnpm build

FROM maven:3.9.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY backend/ ./
COPY --from=admin-build /app/backend/src/main/resources/static/admin ./src/main/resources/static/admin
COPY deploy/maven/settings.xml /tmp/maven-settings.xml
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -s /tmp/maven-settings.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy AS runtime
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --home-dir /app app
WORKDIR /app
COPY --from=backend-build /app/target/mz-blog-*.jar /app/app.jar
RUN mkdir -p /data/uploads && chown -R app:app /app /data
USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=Asia/Shanghai"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

# Xiaoyu Docker Deployment

这个项目已经配置为 Docker Compose 一键部署，包含应用本体以及运行所需的 MySQL、Redis、RabbitMQ、Elasticsearch。

## 环境要求

- Docker
- Docker Compose v2

检查命令：

```bash
docker version
docker compose version
```

## 服务组成

| 服务 | 容器名 | 端口 | 说明 |
| --- | --- | --- | --- |
| xiaoyu | xiaoyu | 8081 | Spring Boot 应用 |
| MySQL | xiaoyu-mysql | 3306 | 业务数据库 |
| Redis | xiaoyu-redis | 6379 | 缓存 |
| RabbitMQ | xiaoyu-rabbitmq | 5672 / 15672 | 消息队列和管理后台 |
| Elasticsearch | xiaoyu-elasticsearch | 9200 | 搜索服务 |

## 启动

在项目根目录执行：

```bash
docker compose up -d --build
```

第一次启动会拉取镜像、编译项目并初始化数据库，耗时会比较久。

查看容器状态：

```bash
docker compose ps
```

查看应用日志：

```bash
docker compose logs -f xiaoyu
```

## 访问地址

应用接口：

```text
http://服务器IP:8081/api
```

RabbitMQ 管理后台：

```text
http://服务器IP:15672
```

默认账号：

```text
admin
```

默认密码：

```text
admin
```

如果部署在云服务器，需要在安全组或防火墙中放行对应端口，例如 `8081` 和 `15672`。

## 数据持久化

Compose 已经配置了 Docker named volumes，不需要手动创建挂载目录。

| 数据卷 | 用途 |
| --- | --- |
| mysql-data | MySQL 数据 |
| redis-data | Redis 数据 |
| rabbitmq-data | RabbitMQ 数据 |
| elasticsearch-data | Elasticsearch 数据 |

查看数据卷：

```bash
docker volume ls
```

普通停止服务：

```bash
docker compose down
```

不要在有重要数据时执行：

```bash
docker compose down -v
```

这个命令会删除数据卷，MySQL、Redis、RabbitMQ、Elasticsearch 的数据都会被清空。

## 数据库初始化

项目根目录的 `mydb_structure.sql` 会挂载到 MySQL 初始化目录：

```text
/docker-entrypoint-initdb.d/01-schema.sql
```

它只会在 `mysql-data` 数据卷第一次创建时自动执行。也就是说：

- 第一次 `docker compose up -d --build` 会初始化数据库
- 后续重启不会重复执行 SQL
- 如果需要重新初始化数据库，需要先删除旧数据卷

谨慎重置数据库：

```bash
docker compose down -v
docker compose up -d --build
```

## 配置说明

应用容器通过 `SPRING_APPLICATION_JSON` 覆盖容器环境下的连接地址：

```text
MySQL: mysql:3306
Redis: redis:6379
RabbitMQ: rabbitmq:5672
Elasticsearch: http://elasticsearch:9200
```

这些服务名来自 `docker-compose.yml`，容器之间会通过 Docker Compose 内部网络自动解析。

## 常用命令

重新构建并启动：

```bash
docker compose up -d --build
```

停止服务：

```bash
docker compose down
```

重启应用：

```bash
docker compose restart xiaoyu
```

查看所有日志：

```bash
docker compose logs -f
```

查看指定服务日志：

```bash
docker compose logs -f mysql
docker compose logs -f redis
docker compose logs -f rabbitmq
docker compose logs -f elasticsearch
docker compose logs -f xiaoyu
```

进入应用容器：

```bash
docker compose exec xiaoyu sh
```

进入 MySQL：

```bash
docker compose exec mysql mysql -uroot -p123456 xiaoyu
```

## 生产环境提醒

当前配置适合测试或内网部署。正式部署前建议：

- 修改 MySQL root 密码
- 修改 RabbitMQ 默认账号和密码
- 不对公网暴露 `3306`、`6379`、`5672`、`9200`
- 只开放应用端口 `8081`，按需开放 RabbitMQ 管理端口 `15672`
- 将敏感配置迁移到 `.env` 或服务器环境变量

## Cloud Server Files

Some deployment files are ignored by Git because they may contain environment-specific values or sensitive data. If the cloud server is deployed with `git pull`, upload these files manually before running `docker compose up -d --build`.

Required files:

```text
mydb_structure.sql
xiaoyu-common/src/main/resources/application.yml
xiaoyu-jiang/src/main/resources/application_j.yml
xiaoyu-jiang/src/main/resources/application_j-dev.yml
xiaoyu-server/src/main/resources/application.yml
xiaoyu-server/src/main/resources/application-dev.yml
```

Optional but recommended when building the image on the server:

```text
.mvn/settings.xml
```

If `.mvn/settings.xml` is used for Maven mirrors during Docker build, add this line before the first `RUN mvn ...` command in `Dockerfile`:

```dockerfile
COPY .mvn/settings.xml /root/.m2/settings.xml
```

Notes:

- `mydb_structure.sql` is mounted by `docker-compose.yml` into the MySQL init directory and only runs when the `mysql-data` volume is created for the first time.
- The SQL file may contain both table structure and seed/test data. It is OK to delete business/test `INSERT INTO` rows before deployment, but keep `CREATE TABLE`, indexes, constraints, and any required dictionary/admin/bootstrap data.
- `xiaoyu-server` depends on `xiaoyu-jiang` and `xiaoyu-common`, so resources from dependency modules may also be packaged into the final application. Do not assume only one YAML file matters unless the Spring config import/profile relationship has been simplified.
- Current Docker Compose starts the application with `SPRING_PROFILES_ACTIVE=dev`, so `application-dev.yml` and imported YAML files must exist during build/runtime unless configuration is moved to environment variables or a production profile.

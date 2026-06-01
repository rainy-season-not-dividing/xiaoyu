# 使用官方 OpenJDK 17 基础镜像（包含完整 JDK）
FROM openjdk:17-jdk-slim

# 设置时区为上海（避免日志时间不对）
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 设置工作目录
WORKDIR /app

# 先复制依赖（利用 Docker 缓存层，加快构建）
COPY target/*.jar app.jar

# 暴露应用端口（根据你的 application.yml 调整）
EXPOSE 8081

# 健康检查（可选，Docker 会自动检测容器状态）
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# 启动命令（优化 JVM 参数）
ENTRYPOINT ["java", \
  "-Xms512m", \
  "-Xmx512m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
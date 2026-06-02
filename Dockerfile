FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY .mvn/settings.xml /root/.m2/settings.xml

COPY pom.xml .
COPY xiaoyu-common/pom.xml xiaoyu-common/pom.xml
COPY xiaoyu-jiang/pom.xml xiaoyu-jiang/pom.xml
COPY xiaoyu-server/pom.xml xiaoyu-server/pom.xml

RUN mvn -B -ntp dependency:go-offline

COPY . .
RUN mvn -B -ntp clean package -DskipTests -pl xiaoyu-server -am

FROM eclipse-temurin:21-jre

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms512m -Xmx512m" \
    SPRING_PROFILES_ACTIVE=dev

WORKDIR /app

COPY --from=build /workspace/xiaoyu-server/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dserver.address=0.0.0.0 -jar app.jar"]

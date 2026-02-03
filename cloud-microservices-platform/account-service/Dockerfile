# Account Service Dockerfile
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY target/*.jar app.jar

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

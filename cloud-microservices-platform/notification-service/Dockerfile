# Notification Service Dockerfile
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY target/*.jar app.jar

ENV JAVA_OPTS="-Xmx256m -Xms128m"
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8084

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

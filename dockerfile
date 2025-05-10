FROM openjdk:17-jdk-alpine
COPY target/trading_analytics-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

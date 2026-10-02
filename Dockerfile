FROM eclipse-temurin:21-jre

COPY target/backend-app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

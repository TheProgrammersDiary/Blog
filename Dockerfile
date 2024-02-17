FROM adoptopenjdk/openjdk17:alpine-jre

WORKDIR /app
COPY blog/target/blog*.jar /app/blog.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/blog.jar"]
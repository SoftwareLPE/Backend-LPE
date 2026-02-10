FROM gradle:8.14.3-jdk21 AS build
WORKDIR /home/gradle/project
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app_backendLPE.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app_backendLPE.jar"]

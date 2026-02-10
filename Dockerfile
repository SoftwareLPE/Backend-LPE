FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app_backendLPE.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app_backendLPE.jar"]

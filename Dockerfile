FROM eclipse-temurin:21-jre
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app_backendLPE.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app_backendLPE.jar"]

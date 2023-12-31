FROM --platform=linux/amd64 maven:3.9.2-eclipse-temurin-17 AS builder
#create folder and subfolders
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME

## verify pom.xml dependencies
ADD pom.xml $HOME
## RUN apk add --no-cache python3 g++ make
RUN mvn verify --fail-never
## add all source code and start compiling
## compile and package to JAR
ADD . $HOME
## Build using the cache
RUN --mount=type=cache,target=/root/.m2,rw mvn package

FROM --platform=linux/amd64 eclipse-temurin:17.0.7_7-jre-jammy
COPY --from=builder /usr/app/target/*.jar /app.jar

EXPOSE 8090
# ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app.jar"]
CMD ["java", "-Dspring.profiles.active=deployment", "-jar", "/app.jar"]
FROM --platform=$BUILDPLATFORM maven:3.9.2-eclipse-temurin-17 AS builder
#create folder and subfolders
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
# verify pom.xml dependencies
ADD pom.xml $HOME
RUN mvn verify --fail-never
# add all source code and start compiling
#compile and package to JAR
ADD . $HOME
#RUN mvn package
# Build using the cache
RUN --mount=type=cache,target=/root/.m2,rw mvn package

FROM --platform=$BUILDPLATFORM eclipse-temurin:17.0.7_7-jre-jammy
COPY --from=builder /usr/app/target/*.jar /app.jar

EXPOSE 8080
# ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app.jar"]
CMD ["java", "-Dspring.profiles.active=docker", "-jar", "/app.jar"]
#FROM eclipse-temurin:17.0.7_7-jre-jammy
#WORKDIR /app
#
#COPY .mvn/ .mvn
#COPY mvnw pom.xml ./
#RUN ./mvnw dependency:go-offline
#
#COPY src ./src
#EXPOSE 8080
#CMD ["./mvnw", "spring-boot:run"]

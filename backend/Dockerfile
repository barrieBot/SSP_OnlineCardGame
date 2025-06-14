FROM maven:3.8.6-openjdk-23

WORKDIR .

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:23-jdk

WORKDIR .

COPY .//target/CardGame-0.0.1.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "./app.jar"]
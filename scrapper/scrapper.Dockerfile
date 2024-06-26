FROM openjdk:21-jdk as base
COPY scrapper/target/scrapper.jar .

#server
EXPOSE 8080

#metrics
EXPOSE 8081

ENTRYPOINT java -jar *.jar

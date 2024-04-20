FROM openjdk:21-jdk as base
COPY bot/target/bot.jar .

#server
EXPOSE 8090

#metrics
EXPOSE 8091

ENTRYPOINT java -jar *.jar

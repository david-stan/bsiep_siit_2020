FROM openjdk:8-jdk-alpine

WORKDIR /app
COPY . /app

RUN javac /app/src/client/ClientSocketApp.java

CMD ["java", "-classpath", "/app/src", "client.ClientSocketApp"]
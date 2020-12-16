FROM amazoncorretto:8

WORKDIR '/custom-server'

COPY . .

RUN javac Server.java

ENV TZ Europe/Kiev

EXPOSE 8080

ENTRYPOINT java Server

FROM amazoncorretto:8

WORKDIR '/custom-server'

COPY . .

RUN javac Server.java

ENTRYPOINT java Server

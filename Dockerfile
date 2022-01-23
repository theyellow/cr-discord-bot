FROM eclipse-temurin:17.0.1_12-jre-alpine
VOLUME /tmp

# timezone env with default
ENV TZ Europe/Berlin

RUN apk -U --no-cache upgrade
COPY target/crdiscordbot.jar crbot.jar
ENTRYPOINT ["java", \
"-Xmx300m", \
"-jar", \
"/crbot.jar"]
EXPOSE 8081
FROM eclipse-temurin:21-alpine
VOLUME /tmp

# timezone env with default
ENV TZ Europe/Berlin

RUN apk -U --no-cache upgrade
COPY target/crdiscordbot-leader.jar crdiscordbot-leader.jar
ENTRYPOINT ["java", \
"-Xmx300m", \
"-jar", \
"/crdiscordbot-leader.jar"]
EXPOSE 8080
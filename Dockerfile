FROM digitalgenius/alpine-jdk:latest
VOLUME /tmp
# timezone env with default
ENV TZ Europe/Berlin
ARG JAR_FILE
RUN apk -U --no-cache upgrade
COPY target/${JAR_FILE} crbot.jar
ENTRYPOINT ["java", \
"-Xmx300m", \
"-jar", \
"/crbot.jar"]
EXPOSE 8081
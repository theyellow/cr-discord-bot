FROM digitalgenius/alpine-jdk:latest
VOLUME /tmp

# timezone env with default
ENV TZ Europe/Berlin
ENV JAR_FILE $(find .. |grep jar |grep crdiscordbot|grep -v original)

RUN apk -U --no-cache upgrade
COPY ${JAR_FILE} crbot.jar
ENTRYPOINT ["java", \
"-Xmx300m", \
"-jar", \
"/crbot.jar"]
EXPOSE 8081
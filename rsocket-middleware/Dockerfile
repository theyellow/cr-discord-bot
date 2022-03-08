FROM eclipse-temurin:17.0.1_12-jre-alpine
VOLUME /tmp

# timezone env with default
ENV TZ Europe/Berlin

RUN apk -U --no-cache upgrade
COPY target/rsocket-middleware.jar rsocket-middleware.jar
ENTRYPOINT ["java", \
"-jar", \
"/rsocket-middleware.jar"]
EXPOSE 8080
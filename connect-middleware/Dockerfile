FROM eclipse-temurin:21-alpine
VOLUME /tmp

# timezone env with default
ENV TZ Europe/Berlin

RUN apk -U --no-cache upgrade
COPY target/connect-middleware-0.0.1-RC1.jar connect-middleware.jar
ENTRYPOINT ["java", \
"-jar", \
"/connect-middleware.jar"]
EXPOSE 8080
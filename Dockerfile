FROM openjdk:8-jdk-alpine
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
ENTRYPOINT java -Dzipkin.base-url=${ZIPKIN:-http://zipkin:9411} -Dzipkin.limit-per-request=${TRACE_LIMIT:-1000} -jar /app.jar
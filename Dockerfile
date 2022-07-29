FROM openjdk:11
ENV APP_HOME=/app
WORKDIR $APP_HOME
COPY /target/*.jar /app/app.jar
COPY /newrelic.yml /
RUN wget https://s3.amazonaws.com/www.vegapay.tech/newrelic.jar
EXPOSE 8086
ENV NEW_RELIC_APP_NAME="charges-processing"
ENV NEW_RELIC_LICENSE_KEY="ae5b257920bc3d46081b374123e1b16e9fe7NRAL"
ENTRYPOINT ["java","-javaagent:newrelic.jar" ,"-jar", "/app/app.jar"]
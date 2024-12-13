# Базовый образ для сборки кастомной JRE
FROM openjdk:17-alpine as step_one

# Требуется, чтобы работал strip-debug
RUN apk add --no-cache binutils

# Собираем маленький JRE-образ
RUN $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /customjre

# Главный образ приложения
FROM alpine:latest as step_two
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Копируем JRE из базового образа
COPY --from=step_one /customjre $JAVA_HOME

ENV HIBERNATE_USERNAME=admin HIBERNATE_PASSWORD=admin POSTGRESQL_CONTAINER_NAME=cw_db DB_CONTAINER_PORT=5432 POSTGRES_DB=coworking_db
COPY tomcat /tomcat
COPY cw /tomcat/webapps/cw

EXPOSE 8080
ENTRYPOINT ["/tomcat/bin/catalina.sh"]
CMD ["run"]
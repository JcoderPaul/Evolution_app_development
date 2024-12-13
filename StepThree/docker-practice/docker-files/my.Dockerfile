FROM alpine:3.21
RUN apk add openjdk17-jre

ENV HIBERNATE_USERNAME=admin HIBERNATE_PASSWORD=admin POSTGRESQL_CONTAINER_NAME=cw_db DB_CONTAINER_PORT=5432 POSTGRES_DB=coworking_db
COPY tomcat /tomcat
COPY cw /tomcat/webapps/cw

EXPOSE 8080
ENTRYPOINT ["/tomcat/bin/catalina.sh"]
CMD ["run"]

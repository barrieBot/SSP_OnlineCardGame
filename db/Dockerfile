FROM mysql:latest
ENV MYSQL_ROOT_PASSWORD=password
ENV MYSQL_DATABASE=test
ENV MYSQL_USER=testUser
ENV MYSQL_PASSWORD=12345678

COPY data.sql /docker-entrypoint-initdb.d/

EXPOSE 3306

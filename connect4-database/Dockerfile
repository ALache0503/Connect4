FROM postgres:latest

ENV POSTGRES_USER=user
ENV POSTGRES_PASSWORD=password
ENV POSTGRES_DB=connect4

COPY connect4-database/schema.sql /docker-entrypoint-initdb.d/

EXPOSE 5432
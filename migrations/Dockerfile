FROM flyway/flyway:latest
COPY sql ./sql
ENTRYPOINT ["/bin/sh", "-c", \
     "flyway -url=$DB_URL \
     -user=$DB_USER \
     -password=$DB_PASSWORD \
     -connectRetries=60 \
     migrate"]

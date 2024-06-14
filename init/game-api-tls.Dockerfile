FROM openjdk:17-alpine
ARG SSL_KS_PASSWORD
WORKDIR /var/tmp/ks
COPY certs/game-api.jks .

# RUN openssl pkcs12 -export \
#     -in fullchain.pem \
#     -inkey privkey.pem \
#     -name game-api \
#     -password pass:$SSL_KS_PASSWORD > game-api.p12
#
# RUN keytool -importkeystore \
#     -srckeystore game-api.p12 \
#     -destkeystore game-api.jks \
#     -srcstoretype pkcs12 \
#     -alias game-api \
#     -deststorepass $SSL_KS_PASSWORD \
#     -srcstorepass $SSL_KS_PASSWORD

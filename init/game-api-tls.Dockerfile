FROM openjdk:17-alpine
ARG STOREPASS
WORKDIR /var/tmp/ks
RUN keytool -genkeypair \
    -keyalg RSA \
    -keysize 2048 \
    -alias game-api \
    -keystore game-api.jks \
    -storetype pkcs12 \
    -validity 365 \
    -storepass $STOREPASS \
    -dname "CN=ll, OU=ll, O=ll, L=ll, S=ll, C=ll"
WORKDIR /var/tmp/cert
RUN keytool -export \
    -alias game-api \
    -keystore /var/tmp/ks/game-api.jks \
    -storepass $STOREPASS \
    -file game-api-cert.pem \
    -rfc

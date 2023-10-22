FROM openjdk:17-alpine
ARG SSL_KS_PASSWORD
WORKDIR /var/tmp/ks
RUN keytool -genkeypair \
    -keyalg RSA \
    -keysize 2048 \
    -alias game-api \
    -keystore game-api.jks \
    -storetype pkcs12 \
    -validity 365 \
    -storepass $SSL_KS_PASSWORD \
    -dname "CN=ll, OU=ll, O=ll, L=ll, S=ll, C=ll"
WORKDIR /var/tmp/cert
RUN keytool -export \
    -alias game-api \
    -keystore /var/tmp/ks/game-api.jks \
    -storepass $SSL_KS_PASSWORD \
    -file game-api-cert.pem \
    -rfc

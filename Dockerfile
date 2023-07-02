FROM eclipse-temurin:17.0.7_7-jre

WORKDIR /var/keys/
RUN openssl genrsa -out jwtRSA256.pem 2048
RUN openssl rsa -in jwtRSA256.pem -pubout -outform PEM -out jwtRSA256.pem.pub
RUN chown 1001:0 jwtRSA256.pem
CMD []

FROM alpine/openssl
WORKDIR /var/keys/
RUN openssl genrsa -out jwtRSA256.pem 2048
RUN openssl rsa -in jwtRSA256.pem -pubout -outform PEM -out jwtRSA256.pem.pub
ENTRYPOINT chown 1001:0 jwtRSA256.pem

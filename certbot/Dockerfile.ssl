FROM alpine/openssl
WORKDIR /var/ssl/
RUN openssl dhparam -out /var/ssl/dhparam.pem 4096
ENTRYPOINT chown 1001:0 -R /var/certs

FROM alpine/openssl
ARG REGISTRY_KEY_PASSWORD
WORKDIR /var/certs
RUN openssl genrsa -des3 -out registry.key -passout pass:$REGISTRY_KEY_PASSWORD 2048 && \
    openssl req -key registry.key -new -x509 -days 365 -out registry.crt -passin pass:$REGISTRY_KEY_PASSWORD -subj="/"

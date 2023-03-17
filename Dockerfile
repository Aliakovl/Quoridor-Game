FROM alpine:latest

COPY configs /conf
WORKDIR /conf

CMD ls -l

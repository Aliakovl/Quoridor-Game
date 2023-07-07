FROM nginx:latest
WORKDIR /etc/nginx
COPY ./certbot/conf ./ssl
COPY ./prod/nginx.conf .
COPY ./snippets ./snippets
RUN openssl dhparam -out /etc/nginx/ssl/dhparam.pem 4096

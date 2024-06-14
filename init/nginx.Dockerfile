FROM nginx:latest
COPY ./nginx.conf /etc/nginx/
COPY ./ssl-params.conf /etc/nginx/

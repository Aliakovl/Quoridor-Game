FROM nginx:latest
COPY ./nginx.conf /etc/nginx/
COPY ./ssl-params.conf /etc/nginx/
COPY ./rootCA.pem /etc/nginx/

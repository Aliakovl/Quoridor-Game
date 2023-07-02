FROM node:lts-alpine AS builder
WORKDIR /home
COPY ./ ./
ARG NODE_ENV_ARG
ENV NODE_ENV=$NODE_ENV_ARG
RUN npm install && npm run build
CMD []

FROM node:lts-alpine
WORKDIR /home/node
COPY --from=builder /home/build ./app
COPY --from=builder /home/package.json ./
EXPOSE 3000
CMD ["node", "app"]

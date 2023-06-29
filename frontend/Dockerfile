FROM node:lts-alpine AS builder
WORKDIR /home
COPY ./ ./
RUN npm install && npm run build

FROM node:lts-alpine
WORKDIR /home/node
ENV NODE_ENV=production
COPY --from=builder /home/build ./app
COPY --from=builder /home/package.json ./
EXPOSE 3000
CMD ["node", "app"]
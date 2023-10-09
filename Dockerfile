FROM sbtscala/scala-sbt:eclipse-temurin-focal-17.0.8.1_1_1.9.6_3.3.1 AS builder
LABEL stage=builder
WORKDIR /app
COPY ./src ./src
COPY ./build.sbt .
COPY ./project ./project
RUN ["sbt", "Docker/stage"]
CMD []

FROM alpine
LABEL stage=builder
WORKDIR /build
COPY --from=builder /app/target/docker/stage .
RUN chown -R 1001:0 .
CMD ["echo", "done"]

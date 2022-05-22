FROM hseeberger/scala-sbt:17.0.2_1.6.2_2.13.8
COPY . /root
WORKDIR /root
EXPOSE 8080
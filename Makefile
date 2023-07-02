local: image
	docker-compose --env-file .env.dev up --build

dev:
	@cd frontend && npm run dev

frontend-build:
	@cd frontend && npm run build

init: local-keys frontend-init

frontend-init:
	@cd frontend && npm install

local-keys:
	mkdir -p keys/local
	openssl genrsa -out keys/local/jwtRSA256.pem 2048
	openssl rsa -in keys/local/jwtRSA256.pem -pubout -outform PEM -out keys/local/jwtRSA256.pem.pub
	chmod 644 keys/local/jwtRSA256.pem

image:
	sbt "Docker / stage"

prod:
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml up --build -d

down:
	docker-compose down

runtime-image:
	docker build --no-cache --force-rm -t "quoridor-runtime:17.0.7" ./

backend-image: runtime-image
	sbt "Docker/publishLocal"

frontend-image:
	docker build --no-cache --force-rm -t "quoridor-frontend:latest" --build-arg NODE_ENV_ARG=development frontend/

build-and-run: backend-image frontend-image
	docker-compose -f docker-compose.local.yml --env-file .env.dev up --build

delete-builder-images:
	docker rmi $(docker images | grep "<none>" | awk '{print $3}')

init-dev: init-keys init-frontend dev-containers

init-keys: build-runtime-image
	docker create --name quoridor-keys quoridor-runtime echo
	docker cp quoridor-keys:/var/keys/ keys/
	docker rm -f quoridor-keys

init-frontend:
	cd frontend && npm install

dev-containers:
	docker-compose -f docker-compose.dev.yml --env-file .env.dev up --build -d

dev: dev-backend dev-frontend

dev-frontend:
	cd frontend && npm run dev

dev-backend:
	sbt run

local: build-backend-image build-frontend-image
	docker-compose -f docker-compose.local.yml --env-file .env.dev up --build -d

build-runtime-image:
	docker build --no-cache --force-rm -t "quoridor-runtime" ./

build-backend-image: build-runtime-image
	sbt "Docker/publishLocal"

build-frontend-image:
	docker build --no-cache --force-rm -t "quoridor-frontend:latest" --build-arg NODE_ENV_ARG=development frontend/

delete-builder-images:
	docker rmi $$(docker images | grep '<none>' | awk '{print $$3}')

down:
	docker-compose down
	docker stop $$(docker ps -q) | true
	docker rm $$(docker ps -aq) | true

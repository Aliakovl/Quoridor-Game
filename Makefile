VERSION := $(shell git log -1 --pretty=tformat:"%h" master)
DOCKER_REGISTRY = quoridor.online:5000
DOCKER_CONTEXT = quoridor

init-dev: init-keys init-frontend containers-dev

init-keys: build-runtime-image
	docker create --name quoridor-keys quoridor-runtime echo
	docker cp quoridor-keys:/var/keys/ keys/
	docker rm -f quoridor-keys

init-frontend:
	cd frontend && npm install

containers-dev:
	docker-compose -f docker-compose.dev.yml --env-file .env.dev up --build -d

frontend-dev:
	cd frontend && npm run dev

backend-dev:
	export $$(cat .env.dev) && sbt run

local: build-backend build-frontend-dev
	docker-compose -f docker-compose.local.yml --env-file .env.dev up --build -d

build-runtime-image:
	docker build --no-cache --force-rm -t "quoridor-runtime" ./

build-backend: build-runtime-image
	sbt "Docker/publishLocal"

build-frontend-dev:
	docker build --no-cache --force-rm -t "quoridor-frontend" --build-arg NODE_ENV_ARG=development frontend/

build-frontend:
	docker build --no-cache --force-rm -t "quoridor-frontend" --build-arg NODE_ENV_ARG=production frontend/

delete-builders:
	docker rmi $$(docker images | grep '<none>' | awk '{print $$3}')

down:
	docker-compose down
	docker stop $$(docker ps -q) | true
	docker rm $$(docker ps -aq) | true
	docker-compose down

build-config:
	docker build -t "quoridor-config" configs

build-nginx:
	docker build -t "quoridor-nginx" --progress=plain nginx/

build: build-config build-backend build-nginx build-frontend

publish-config:
	docker image tag quoridor-config:latest $(DOCKER_REGISTRY)/quoridor-config:$(VERSION)
	docker image tag quoridor-config:latest $(DOCKER_REGISTRY)/quoridor-config:latest
	docker image push $(DOCKER_REGISTRY)/quoridor-config:$(VERSION)
	docker image push $(DOCKER_REGISTRY)/quoridor-config:latest

publish-game:
	docker image tag quoridor-game:latest $(DOCKER_REGISTRY)/quoridor-game:$(VERSION)
	docker image tag quoridor-game:latest $(DOCKER_REGISTRY)/quoridor-game:latest
	docker image push $(DOCKER_REGISTRY)/quoridor-game:$(VERSION)
	docker image push $(DOCKER_REGISTRY)/quoridor-game:latest

publish-frontend:
	docker image tag quoridor-frontend:latest $(DOCKER_REGISTRY)/quoridor-frontend:$(VERSION)
	docker image tag quoridor-frontend:latest $(DOCKER_REGISTRY)/quoridor-frontend:latest
	docker image push $(DOCKER_REGISTRY)/quoridor-frontend:$(VERSION)
	docker image push $(DOCKER_REGISTRY)/quoridor-frontend:latest

publish-nginx:
	docker image tag quoridor-nginx:latest $(DOCKER_REGISTRY)/quoridor-nginx:$(VERSION)
	docker image tag quoridor-nginx:latest $(DOCKER_REGISTRY)/quoridor-nginx:latest
	docker image push $(DOCKER_REGISTRY)/quoridor-nginx:$(VERSION)
	docker image push $(DOCKER_REGISTRY)/quoridor-nginx:latest

deploy:
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
 	docker-compose -f docker-compose.prod.yml --env-file .env up -d

version:
	@echo $(VERSION)

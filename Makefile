VERSION := "$(shell date +'%Y.%m.%d')-$(shell git log -1 --pretty=tformat:"%h")"
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
	export $$(cat .env.dev) && sbt "compile; run"

local: build-backend-dev build-frontend-dev
	docker-compose -f docker-compose.local.yml --env-file .env.dev up --build -d

build-runtime-image:
	docker build --no-cache --force-rm -t "quoridor-runtime" ./runtime

build-backend-dev: build-runtime-image
	sbt "Docker/publishLocal"

build-frontend-dev:
	docker build --no-cache --force-rm -t "quoridor-frontend" --build-arg NODE_ENV_ARG=development frontend/
	docker image prune --filter label=stage=builder

build-backend: build-runtime-image
	docker build --no-cache -t "quoridor-build" .
	docker run --name "quoridor-build" "quoridor-build"
	docker cp quoridor-build:/build/ ./build
	docker rm "quoridor-build"
	docker build --no-cache -t "quoridor-game" build
	docker image prune -f --filter label=stage=builder
	docker image rm -f quoridor-build
	docker image prune -f --filter label=snp-multi-stage=intermediate

build-frontend:
	docker build --no-cache --force-rm -t "quoridor-frontend" --build-arg NODE_ENV_ARG=production frontend/
	docker image prune --filter label=stage=builder

delete-builders:
	docker rmi $$(docker images | grep '<none>' | awk '{print $$3}')

down:
	docker-compose -f "docker-compose.dev.yml" -f "docker-compose.local.yml" -f "docker-compose.prod.yml" down
	docker stop $$(docker ps -q) | true
	docker rm $$(docker ps -aq) | true
	docker-compose -f "docker-compose.dev.yml" -f "docker-compose.local.yml" -f "docker-compose.prod.yml" down

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

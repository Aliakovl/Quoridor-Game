VERSION := $(shell TZ=UTC-3 date +'%Y.%m.%d')-$(shell git log -1 --pretty=tformat:"%h")
DOCKER_REGISTRY := $(shell awk -F= '{ if ($$1 == "DOCKER_REGISTRY") { print $$2 } }' .env)
DOCKER_CONTEXT = $(shell awk -F= '{ if ($$1 == "DOCKER_CONTEXT") { print $$2 } }' .env)
DOCKER_USERNAME = $(shell awk -F= '{ if ($$1 == "DOCKER_USERNAME") { print $$2 } }' .env)

init-dev: init-keys init-frontend

init-frontend:
	cd frontend && npm install

init-keys:
	docker build -t quoridor/keys --file certbot/jwt-keys.Dockerfile certbot/
	docker volume create secret_keys
	docker run --name quoridor-keys -v secret_keys:/var/keys quoridor/keys
	docker cp quoridor-keys:/var/keys ./keys
	docker rm quoridor-keys
	docker rmi quoridor/keys

run-infra-dev:
	docker-compose -f docker-compose.dev.yml --env-file .env.dev up -d

run-frontend-dev:
	cd frontend && npm run dev

run-game-api-dev:
	export $$(cat .env.dev) && sbt "compile; run"

build-game-api-config-local:
	docker volume rm game-api-config | true
	docker build --no-cache -t $(DOCKER_USERNAME)/game-api-config:local configs

build-game-api-local:
	sbt "Docker/publishLocal"
	docker image prune -f --filter label=snp-multi-stage=intermediate

build-frontend-local:
	docker build --no-cache --force-rm -t $(DOCKER_USERNAME)/frontend:local --build-arg NODE_ENV_ARG=development frontend/
	docker image prune --filter label=stage=builder

build-local: build-game-api-config-local build-migrations build-game-api-local build-frontend-local

local:
	docker-compose -f docker-compose.local.yml --env-file .env.dev up -d

down:
	docker-compose -f "docker-compose.dev.yml" -f "docker-compose.local.yml" -f "docker-compose.prod.yml" down
	docker stop $$(docker ps -q) | true
	docker rm $$(docker ps -aq) | true
	docker-compose -f "docker-compose.dev.yml" -f "docker-compose.local.yml" -f "docker-compose.prod.yml" down

remove:
	$(eval TMP := $(shell docker images -q 'quoridor/*' && docker images -q '*/quoridor/*'))
	docker rmi -f $$(echo $(TMP) | sort -u)

remove-none:
	docker rmi $$(docker images -f "dangling=true" -q | sort -u)

deploy-init:
	@export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	docker-compose -f certbot/docker-compose.yml --env-file .env up --build -d

deploy-registry:
	@export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
    docker-compose -f registry/docker-compose.yml --env-file .env up -d

build-game-api:
	docker build --no-cache -t quoridor/build .
	docker run --name quoridor-build quoridor/build
	docker cp quoridor-build:/build .
	docker rm quoridor-build
	docker build --no-cache -t $(DOCKER_USERNAME)/game-api ./build
	docker image prune -f --filter label=stage=builder
	docker image rm -f quoridor-build
	docker image prune -f --filter label=snp-multi-stage=intermediate

build-frontend:
	docker build --no-cache --force-rm -t $(DOCKER_USERNAME)/frontend --build-arg NODE_ENV_ARG=production frontend/
	docker image prune --filter label=stage=builder

build-game-api-config:
	docker volume rm game-api-config | true
	docker build --no-cache -t $(DOCKER_USERNAME)/game-api-config configs

build-migrations:
	docker build --no-cache -t $(DOCKER_USERNAME)/migrations migrations

build-nginx:
	docker build --no-cache -t $(DOCKER_USERNAME)/nginx nginx

build: build-game-api-config build-migrations build-game-api build-nginx build-frontend

publish-config:
	docker image tag $(DOCKER_USERNAME)/game-api-config:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/game-api-config:$(VERSION)
	docker image tag $(DOCKER_USERNAME)/game-api-config:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/game-api-config:latest
	docker image push --all-tags $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/game-api-config

publish-migrations:
	docker image tag $(DOCKER_USERNAME)/migrations:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/migrations:$(VERSION)
	docker image tag $(DOCKER_USERNAME)/migrations:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/migrations:latest
	docker image push --all-tags $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/migrations

publish-game:
	docker image tag $(DOCKER_USERNAME)/game-api:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/game-api:$(VERSION)
	docker image tag $(DOCKER_USERNAME)/game-api:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/game-api:latest
	docker image push --all-tags $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/game-api

publish-frontend:
	docker image tag $(DOCKER_USERNAME)/frontend:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/frontend:$(VERSION)
	docker image tag $(DOCKER_USERNAME)/frontend:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/frontend:latest
	docker image push --all-tags $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/frontend

publish-nginx:
	docker image tag $(DOCKER_USERNAME)/nginx:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/nginx:$(VERSION)
	docker image tag $(DOCKER_USERNAME)/nginx:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/nginx:latest
	docker image push --all-tags $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/nginx

deploy:
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
 	docker-compose -f docker-compose.prod.yml --env-file .env up -d

deploy-game:
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	docker-compose -f docker-compose.prod.yml --env-file .env up -d migrations && \
	docker volume rm game-api-config && \
	docker-compose -f docker-compose.prod.yml --env-file .env up -d game-api-config && \
	docker-compose -f docker-compose.prod.yml --env-file .env up -d game-api

deploy-frontend:
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	docker-compose -f docker-compose.prod.yml --env-file .env up -d frontend

deploy-nginx:
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	docker-compose -f docker-compose.prod.yml --env-file .env up -d nginx

down-prod:
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	docker-compose -f docker-compose.prod.yml --env-file .env down

version:
	@echo $(VERSION)

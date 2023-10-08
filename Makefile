VERSION := $(shell TZ=UTC-3 date +'%Y.%m.%d')-$(shell git log -1 --pretty=tformat:"%h")
DOCKER_REGISTRY = quoridor.online:5000
DOCKER_CONTEXT = quoridor
DOCKER_USERNAME = quoridor

init-dev: init-frontend containers-dev

init-frontend:
	cd frontend && npm install

containers-dev:
	docker-compose -f docker-compose.dev.yml --env-file .env.dev up -d

frontend-dev:
	cd frontend && npm run dev

backend-dev:
	export $$(cat .env.dev) && sbt "compile; run"

local: build-config build-migrations build-backend-dev build-frontend-dev
	docker-compose -f docker-compose.local.yml --env-file .env.dev up -d

build-backend-dev:
	sbt "Docker/publishLocal"
	docker image prune -f --filter label=snp-multi-stage=intermediate

build-frontend-dev:
	docker build --no-cache --force-rm -t $(DOCKER_USERNAME)/frontend --build-arg NODE_ENV_ARG=development frontend/
	docker image prune --filter label=stage=builder

build-backend:
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

remove-none:
	docker rmi $$(docker images -f "dangling=true" -q | sort -u)

down:
	docker-compose -f "docker-compose.dev.yml" -f "docker-compose.local.yml" -f "docker-compose.prod.yml" down
	docker stop $$(docker ps -q) | true
	docker rm $$(docker ps -aq) | true
	docker-compose -f "docker-compose.dev.yml" -f "docker-compose.local.yml" -f "docker-compose.prod.yml" down

remove:
	$(eval TMP := $(shell docker images -q 'quoridor/*' && docker images -q '*/quoridor/*'))
	docker rmi -f $$(echo $(TMP) | sort -u)

build-config:
	docker volume rm quoridor-game_conf | true
	docker build --no-cache -t $(DOCKER_USERNAME)/game-api-config configs

build-migrations:
	docker build --no-cache -t $(DOCKER_USERNAME)/migrations migrations

build-nginx:
	docker build --no-cache -t $(DOCKER_USERNAME)/nginx nginx

build: build-config build-migrations build-backend build-nginx build-frontend

publish-config:
	docker image tag $(DOCKER_USERNAME)/config:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/config:$(VERSION)
	docker image tag $(DOCKER_USERNAME)/config:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/config:latest
	docker image push --all-tags $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/config

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
	docker-compose -f docker-compose.prod.yml --env-file .env up -d flyway && \
	docker volume rm quoridor-game_conf && \
	docker-compose -f docker-compose.prod.yml --env-file .env up -d game-api-conf && \
	docker-compose -f docker-compose.prod.yml --env-file .env up -d game-api

deploy-frontend:
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	docker-compose -f docker-compose.prod.yml --env-file .env up -d frontend

deploy-nginx:
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	docker-compose -f docker-compose.prod.yml --env-file .env up -d nginx

deploy-init:
	@export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	docker-compose -f certbot/docker-compose.yml --env-file .env up --build -d

deploy-registry:
	@export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
    docker-compose -f registry/docker-compose.yml --env-file .env up -d

down-prod:
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	docker-compose -f docker-compose.prod.yml --env-file .env down

version:
	@echo $(VERSION)

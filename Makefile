VERSION = $(shell TZ=UTC-3 date +'%Y.%m.%d')-$(shell git log -1 --pretty=tformat:"%h")
DOCKER_REGISTRY = $(shell awk -F= '{ if ($$1 == "DOCKER_REGISTRY") { print $$2 } }' .env)
DOCKER_CONTEXT = $(shell awk -F= '{ if ($$1 == "DOCKER_CONTEXT") { print $$2 } }' .env)
DOCKER_USERNAME = $(shell awk -F= '{ if ($$1 == "DOCKER_USERNAME") { print $$2 } }' .env)

init-dev: init-keys init-frontend init-game-api-tls

init-frontend:
	cd frontend && \
	npm install

init-keys:
	docker build -t quoridor/keys --file init/jwt-keys.Dockerfile init/
	docker volume create jwt-keys
	docker run --name quoridor-keys -v jwt-keys:/var/keys quoridor/keys
	mkdir -p .var
	docker cp quoridor-keys:/var/keys ./.var/keys
	docker rm quoridor-keys
	docker rmi quoridor/keys

init-game-api-tls:
	$(eval SSL_KS_PASSWORD = $(shell awk -F= '{ if ($$1 == "SSL_KS_PASSWORD") { print $$2 } }' .env.dev))
	docker build -t quoridor/game-api-tls --build-arg SSL_KS_PASSWORD=$(SSL_KS_PASSWORD) --file init/game-api-tls.Dockerfile init/
	docker volume create game-api-jks
	docker volume create game-api-tls
	docker run --name quoridor-game-api-tls -v game-api-jks:/var/tmp/ks -v game-api-tls:/var/tmp/cert quoridor/game-api-tls
	mkdir -p .var
	docker cp quoridor-game-api-tls:/var/tmp/ks ./.var/game-api-jks
	docker cp quoridor-game-api-tls:/var/tmp/cert ./.var/game-api-tls
	docker rm quoridor-game-api-tls
	docker rmi quoridor/game-api-tls

run-infra-dev:
	docker-compose -f docker-compose.dev.yml --env-file .env.dev up -d

run-frontend-dev:
	cd frontend && \
	npm run dev

run-game-api-dev:
	@export $$(cat .env.dev) && \
	sbt "compile; run"

build-game-api-config-local:
	docker volume rm game-api-config | true
	docker build --no-cache -t $(DOCKER_USERNAME)/game-api-config:local --build-arg ENV=local configs

build-game-api-local:
	sbt "Docker/publishLocal"
	docker image prune -f --filter label=snp-multi-stage=intermediate

build-frontend-local:
	docker build --no-cache --force-rm -t $(DOCKER_USERNAME)/frontend:local --build-arg NODE_ENV_ARG=development frontend/
	docker image prune --filter label=stage=builder

build-local: build-game-api-config-local build-migrations build-game-api-local build-frontend-local

local:
	@export VERSION=$(VERSION) && \
	docker-compose -f docker-compose.local.yml --env-file .env.dev up -d

down:
	docker-compose -f "docker-compose.dev.yml" -f "docker-compose.local.yml" -f "docker-compose.prod.yml" down
	docker stop $$(docker ps -q) | true
	docker rm $$(docker ps -aq) | true
	docker-compose -f "docker-compose.dev.yml" -f "docker-compose.local.yml" -f "docker-compose.prod.yml" down

remove:
	$(eval TMP = $(shell docker images -q 'quoridor/*' && docker images -q '*/quoridor/*'))
	docker rmi -f $$(echo $(TMP) | sort -u)

remove-none:
	docker rmi $$(docker images -f "dangling=true" -q | sort -u)

deploy-registry:
	@export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	docker-compose -f registry/docker-compose.yml --env-file .env up --build -d

build-init:
	$(eval SSL_KS_PASSWORD = $(shell awk -F= '{ if ($$1 == "SSL_KS_PASSWORD") { print $$2 } }' .env))
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	docker-compose -f init/docker-compose.yml --env-file .env build

publish-init:
	docker image push $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/nginx-certbot:latest
	docker image push $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/init-keys:latest
	docker image push $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/init-tls:latest
	docker image push $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/init-game-api-tls:latest

deploy-init:
	@export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	docker-compose -f init/docker-compose.yml --env-file .env up -d

build-game-api:
	rm -r ./.build | true
	docker build --no-cache -t quoridor/build .
	docker run --name quoridor-build quoridor/build
	docker cp quoridor-build:/build ./.build
	docker rm quoridor-build
	docker build --no-cache -t $(DOCKER_USERNAME)/game-api ./.build
	docker image prune -f --filter label=stage=builder
	docker image rm -f quoridor-build
	docker image prune -f --filter label=snp-multi-stage=intermediate

build-frontend:
	docker build --no-cache --force-rm -t $(DOCKER_USERNAME)/frontend --build-arg NODE_ENV_ARG=production frontend/
	docker image prune --filter label=stage=builder

build-game-api-config:
	docker volume rm game-api-config | true
	docker build --no-cache -t $(DOCKER_USERNAME)/game-api-config --build-arg ENV=prod configs

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

publish: publish-config publish-migrations publish-game publish-frontend publish-nginx

deploy:
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	export VERSION=$(VERSION) && \
	docker-compose -f docker-compose.prod.yml --env-file .env up -d

down-prod:
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
	export VERSION=$(VERSION) && \
	docker-compose -f docker-compose.prod.yml --env-file .env down

version:
	@echo $(VERSION)

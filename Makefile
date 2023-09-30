VERSION := $(shell TZ=UTC-3 date +'%Y.%m.%d')-$(shell git log -1 --pretty=tformat:"%h")
DOCKER_REGISTRY = quoridor.online:5000
DOCKER_CONTEXT = quoridor
DOCKER_USERNAME = quoridor

init-dev: init-keys init-frontend containers-dev

init-keys: build-runtime-image
	docker create --name quoridor-keys $(DOCKER_USERNAME)/runtime echo
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

local: build-config build-backend-dev build-frontend-dev
	docker-compose -f docker-compose.local.yml --env-file .env.dev up --build -d

build-runtime-image:
	docker build --no-cache --force-rm -t $(DOCKER_USERNAME)/runtime ./runtime

build-backend-dev: build-runtime-image
	sbt "Docker/publishLocal"
	docker image prune -f --filter label=snp-multi-stage=intermediate

build-frontend-dev:
	docker build --no-cache --force-rm -t $(DOCKER_USERNAME)/frontend --build-arg NODE_ENV_ARG=development frontend/
	docker image prune --filter label=stage=builder

build-backend: build-runtime-image
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
	docker volume rm quoridor-game_conf
	docker build --no-cache -t $(DOCKER_USERNAME)/game-api-config configs

build-nginx:
	docker build --no-cache -t $(DOCKER_USERNAME)/nginx --progress=plain nginx/

build: build-config build-backend build-nginx build-frontend

publish-config:
	docker image tag $(DOCKER_USERNAME)/config:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/config:$(VERSION)
	docker image tag $(DOCKER_USERNAME)/config:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/config:latest
	docker image push --all-tags $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/config:latest

publish-game:
	docker image tag $(DOCKER_USERNAME)/game:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/game:$(VERSION)
	docker image tag $(DOCKER_USERNAME)/game:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/game:latest
	docker image push --all-tags $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/game:latest

publish-frontend:
	docker image tag $(DOCKER_USERNAME)/frontend:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/frontend:$(VERSION)
	docker image tag $(DOCKER_USERNAME)/frontend:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/frontend:latest
	docker image push --all-tags $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/frontend:latest

publish-nginx:
	docker image tag $(DOCKER_USERNAME)/nginx:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/nginx:$(VERSION)
	docker image tag $(DOCKER_USERNAME)/nginx:latest $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/nginx:latest
	docker image push --all-tags $(DOCKER_REGISTRY)/$(DOCKER_USERNAME)/nginx:latest

deploy:
	@export DOCKER_REGISTRY=$(DOCKER_REGISTRY) && \
	export DOCKER_CONTEXT=$(DOCKER_CONTEXT) && \
 	docker-compose -f docker-compose.prod.yml --env-file .env up -d

version:
	@echo $(VERSION)

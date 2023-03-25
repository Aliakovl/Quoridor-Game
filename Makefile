local: export DB_PASSWORD = postgres
local: export DB_USER = postgres
local: export PSWD_PEPPER = pepper
local: export TS_PASSWORD = redis
local: image
	docker-compose up --build

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

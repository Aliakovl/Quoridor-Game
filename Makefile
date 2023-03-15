local: export DB_PASSWORD = postgres
local: export DB_USER = postgres
local: export PSWD_PEPPER = pepper
local: image
	docker-compose up --build

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

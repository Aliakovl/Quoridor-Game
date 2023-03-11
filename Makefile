local: export DB_PASSWORD = postgres
local: export DB_USER = postgres
local: export PSWD_PEPPER = pepper
local: image
	docker-compose up --build

image:
	sbt "Docker / stage"

prod:
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml up --build -d

down:
	docker-compose down
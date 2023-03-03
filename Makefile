local: export DB_PASSWORD = postgres
local: export DB_USER = postgres
local:
	docker-compose up --build

prod:
	docker-compose -f docker-compose.yml -f docker-compose.prod.yml up --build

down:
	docker-compose down
SHELL=/bin/bash -euo pipefail

install:
	 npm install

lint:
	npm run lint

build:
	docker build -t hello-world-sandbox .

run:
	docker run -p 9000:9000 hello-world-sandbox:latest

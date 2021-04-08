SHELL=/bin/bash -euo pipefail

install: install-node install-python install-hooks

install-python:
	poetry install

install-node:
	npm install

install-hooks:
	cp scripts/pre-commit .git/hooks/pre-commit

lint:
	npm run lint
	poetry run flake8 **/*.py

clean:
	rm -rf build
	rm -rf dist

publish: clean
	mkdir -p build
	npm run publish 2> /dev/null

serve:
	npm run serve

check-licenses:
	npm run check-licenses
	scripts/check_python_licenses.sh

deploy-proxy:
	scripts/deploy_proxy.sh

deploy-spec:
	scripts/deploy_spec.sh

format:
	poetry run black **/*.py

build-proxy:
	scripts/build_proxy.sh

release: clean publish build-proxy
	mkdir -p dist
	cp -r build/. dist

	for env in internal-dev internal-qa; do \
		cat ecs-proxies-deploy.yml | sed -e 's/{{ SPINE_ENV }}/veit07/g' -e 's/{{ SANDBOX_MODE_ENABLED }}/False/g' > dist/ecs-deploy-$$env.yml; \
	done

	cat ecs-proxies-deploy.yml | sed -e 's/{{ SPINE_ENV }}/int/g' -e 's/{{ SANDBOX_MODE_ENABLED }}/False/g' > dist/ecs-deploy-int.yml
	cat ecs-proxies-deploy.yml | sed -e 's/{{ SPINE_ENV }}/ref/g' -e 's/{{ SANDBOX_MODE_ENABLED }}/False/g' > dist/ecs-deploy-ref.yml

	for env in internal-dev-sandbox internal-qa-sandbox sandbox; do \
		cp ecs-proxies-deploy-sandbox.yml dist/ecs-deploy-$$env.yml; \
	done

test:
	echo "TODO: add tests"

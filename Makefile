SHELL=/bin/bash -euo pipefail

guard-%:
	@ if [ "${${*}}" = "" ]; then \
		echo "Environment variable $* not set"; \
		exit 1; \
	fi

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

_dist_include="pytest.ini poetry.lock poetry.toml pyproject.toml Makefile build/. tests"

release: clean publish build-proxy
	mkdir -p dist
	for f in $(_dist_include); do cp -r $$f dist; done

	for env in internal-dev internal-qa; do \
		cat ecs-proxies-deploy.yml | sed -e 's/{{ SPINE_ENV }}/veit07/g' -e 's/{{ SANDBOX_MODE_ENABLED }}/False/g' > dist/ecs-deploy-$$env.yml; \
	done

	cat ecs-proxies-deploy.yml | sed -e 's/{{ SPINE_ENV }}/int/g' -e 's/{{ SANDBOX_MODE_ENABLED }}/False/g' > dist/ecs-deploy-int.yml
	cat ecs-proxies-deploy.yml | sed -e 's/{{ SPINE_ENV }}/ref/g' -e 's/{{ SANDBOX_MODE_ENABLED }}/False/g' > dist/ecs-deploy-ref.yml
	cat ecs-proxies-deploy-prod.yml | sed -e 's/{{ SPINE_ENV }}/prod/g' > dist/ecs-deploy-prod.yml

	for env in internal-dev-sandbox internal-qa-sandbox sandbox; do \
		cp ecs-proxies-deploy-sandbox.yml dist/ecs-deploy-$$env.yml; \
	done

dist: release

test: smoketest

pytest-guards: guard-SERVICE_BASE_PATH guard-APIGEE_ENVIRONMENT guard-SOURCE_COMMIT_ID guard-STATUS_ENDPOINT_API_KEY

smoketest: pytest-guards
	poetry run python -m pytest -v --junitxml=smoketest-report.xml -s -m smoketest

import pytest
from .utils.check_oauth import CheckOauth
import uuid
import random
from .configuration import config


@pytest.fixture()
def headers():
    return get_headers() if "sandbox" in config.ENVIRONMENT else get_headers_with_token()


def get_headers_with_token():
    """Assign required headers with the Authorization header"""
    token = get_token()
    headers = {"X-Request-ID": str(uuid.uuid4()),
               "X-Correlation-ID": str(uuid.uuid4()),
               "NHSD-Session-URID": "555254242102",
               "Authorization": f'Bearer {token}'
               }
    return headers


def get_headers():
    """Assign required headers without the Authorization header"""
    headers = {"X-Request-ID": str(uuid.uuid4()),
               "X-Correlation-ID": str(uuid.uuid4()),
               "NHSD-Session-URID": "555254242102"
               }
    return headers

# Python fixtures use v1 auth which APIM have retired.
def get_token():
    """Get an access token"""
    #oauth_endpoints = CheckOauth()
    #token = oauth_endpoints.get_token_response()
    #access_token = token['access_token']
    #return access_token
    return 1


@pytest.fixture()
def create_random_date(request):
    day = str(random.randrange(1, 28)).zfill(2)
    month = str(random.randrange(1, 12)).zfill(2)
    year = random.randrange(1940, 2020)
    new_date = f"{year}-{month}-{day}"
    setattr(request.cls, 'new_date', new_date)

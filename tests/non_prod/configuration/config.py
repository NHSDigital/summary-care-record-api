from ..utils.environment import EnvVarWrapper


ENV = EnvVarWrapper(
    **{
        "environment": "APIGEE_ENVIRONMENT",
        'client_id': 'CLIENT_ID',
        'client_secret': 'CLIENT_SECRET',
        'redirect_uri': 'REDIRECT_URI',
        'service_base_path': 'SERVICE_BASE_PATH',
        'app_restricted_api_key': 'APPLICATION_RESTRICTED_API_KEY',
        'app_restricted_key_id': 'APPLICATION_RESTRICTED_KEY_ID',
        'app_restricted_signing_key': 'APPLICATION_RESTRICTED_SIGNING_KEY'
    }
)

# Api Details
ENVIRONMENT = ENV["environment"]
BASE_URL = f"https://{ENVIRONMENT}.api.service.nhs.uk"  # Apigee proxy url

IDENTITY_SERVICE = "oauth2-no-smartcard" if ENVIRONMENT == "int" else "oauth2"

AUTHORIZE_URL = f"{BASE_URL}/{IDENTITY_SERVICE}/authorize"
TOKEN_URL = f"{BASE_URL}/{IDENTITY_SERVICE}/token"
SIM_AUTH_URL = f"{BASE_URL}/{IDENTITY_SERVICE}/simulated_auth"
CALLBACK_URL = f"{BASE_URL}/{IDENTITY_SERVICE}/callback"


SPINE_HOSTNAME = (
    # This value is the url returned in the patients response payload which reflects a spine environment.
    # internal-qa environment points to spine int environment.
    "https://veit07.api.service.nhs.uk" if ENVIRONMENT == "internal-dev" else "https://int.api.service.nhs.uk"
)

# App details
CLIENT_ID = ENV["client_id"]
CLIENT_SECRET = ENV["client_secret"]
REDIRECT_URI = ENV["redirect_uri"]

# Endpoints
ENDPOINTS = {
    "authorize": AUTHORIZE_URL,
    "token": TOKEN_URL,
    "callback": CALLBACK_URL,
    "sim_auth": SIM_AUTH_URL,
}

SERVICE_BASE_PATH = ENV["service_base_path"]

# Set up app restricted variables
APPLICATION_RESTRICTED_API_KEY = ENV["app_restricted_api_key"]
KEY_ID = ENV["app_restricted_key_id"]
SIGNING_KEY = ENV["app_restricted_signing_key"]

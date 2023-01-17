from ..utils.environment import EnvVarWrapper


ENV = EnvVarWrapper(
    **{
        "environment": "APIGEE_ENVIRONMENT",
        'status_endpoint_api_key': 'STATUS_ENDPOINT_API_KEY',
        'commit_id': 'SOURCE_COMMIT_ID'
    }
)

# Api Details
ENVIRONMENT = ENV["environment"]
BASE_URL = f"https://api.service.nhs.uk"  # Apigee proxy url

API_KEY = ENV["status_endpoint_api_key"]
COMMIT_ID = ENV["commit_id"]

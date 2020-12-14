#!/bin/bash
set -euo pipefail

function copy-secret {
    secretValue="$(
        aws secretsmanager get-secret-value \
        --profile build-scr \
        --secret-id "$1" \
        --query SecretString \
        --output text
    )"

    aws ssm put-parameter \
        --profile build-scr \
        --name "$2" \
        --value "$secretValue" \
        --type SecureString \
        --overwrite
}

# internal dev / veit07 spine env
copy-secret "ptl/client/aws.api.veit07.devspineservices.nhs.uk/key" "/ptl/api-deployment/scr/env-internal-dev/spine/client/key"
copy-secret "ptl/client/aws.api.veit07.devspineservices.nhs.uk/crt" "/ptl/api-deployment/scr/env-internal-dev/spine/client/crt"
copy-secret "ptl/veit07.devspineservices.nhs.uk/root-ca/crt" "/ptl/api-deployment/scr/env-internal-dev/spine/root-ca/crt"
copy-secret "ptl/veit07.devspineservices.nhs.uk/sub-ca/crt" "/ptl/api-deployment/scr/env-internal-dev/spine/sub-ca/crt"

# int / int spine env
copy-secret "ptl/client/aws.api.intspineservices.nhs.uk/key" "/ptl/api-deployment/scr/env-int/spine/client/key"
copy-secret "ptl/client/aws.api.intspineservices.nhs.uk/crt" "/ptl/api-deployment/scr/env-int/spine/client/crt"
copy-secret "ptl/veit07.devspineservices.nhs.uk/root-ca/crt" "/ptl/api-deployment/scr/env-int/spine/root-ca/crt"
copy-secret "ptl/veit07.devspineservices.nhs.uk/sub-ca/crt" "/ptl/api-deployment/scr/env-int/spine/sub-ca/crt"

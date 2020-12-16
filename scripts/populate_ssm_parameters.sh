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

function copy-parameter {
    secretValue="$(
        aws ssm get-parameter \
        --profile build-eps-coordinator \
        --name "$1" \
        --query Parameter.Value \
        --output text
    )"

    aws ssm put-parameter \
        --profile build-eps-coordinator \
        --name "$2" \
        --value "$secretValue" \
        --type String \
        --overwrite
}


# veit07
copy-secret "ptl/client/aws.api.veit07.devspineservices.nhs.uk/key" "/ptl/api-deployment/scr/certs/spine/test/key"
copy-secret "ptl/client/aws.api.veit07.devspineservices.nhs.uk/crt" "/ptl/api-deployment/scr/certs/spine/test/crt"

# int
copy-secret "ptl/client/aws.api.intspineservices.nhs.uk/key" "/ptl/api-deployment/scr/certs/spine/int/key"
copy-secret "ptl/client/aws.api.intspineservices.nhs.uk/crt" "/ptl/api-deployment/scr/certs/spine/int/crt"

# ptl envs root & sub ca
copy-secret "ptl/veit07.devspineservices.nhs.uk/root-ca/crt" "/ptl/api-deployment/scr/certs/nhsd-root-ca/ptl/crt"
copy-secret "ptl/veit07.devspineservices.nhs.uk/sub-ca/crt" "/ptl/api-deployment/scr/certs/nhsd-sub-ca/ptl/crt"

# spine urls by spine env
copy-parameter "/ptl/platform-common/test/host" "/ptl/api-deployment/scr/test/host"
copy-parameter "/ptl/platform-common/int/host" "/ptl/api-deployment/scr/int/host"
copy-parameter "/ptl/platform-common/ref/host" "/ptl/api-deployment/scr/ref/host"

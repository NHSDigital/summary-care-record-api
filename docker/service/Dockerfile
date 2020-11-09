FROM alpine:latest

ENTRYPOINT while [[ 1 == 1 ]]; do \
        echo SCR_SECRET_CLIENT_KEY: $(echo ${SCR_SECRET_CLIENT_KEY} | wc -c);\
        echo SCR_SECRET_CLIENT_CERT: $(echo ${SCR_SECRET_CLIENT_CERT} | wc -c);\
        echo SCR_SECRET_CA_CERTS: $(echo ${SCR_SECRET_CA_CERTS} | wc -c);\
        echo SCR_NHSD_ASID_TO: $(echo ${SCR_NHSD_ASID_TO} | wc -c);\
        echo SCR_PARTY_ID_TO: $(echo ${SCR_PARTY_ID_TO} | wc -c);\
        sleep 1; \
        done

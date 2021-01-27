package uk.nhs.adaptors.scr.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestData {
    private String body;
    private String nhsdAsid;
    private String clientIp;
    private String nhsdIdentity;
    private String nhsdSessionUrid;
    private String authorization;
}

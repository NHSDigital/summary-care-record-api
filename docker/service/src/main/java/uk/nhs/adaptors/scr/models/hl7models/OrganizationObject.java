package uk.nhs.adaptors.scr.models.hl7models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationObject {
    private String idExtension;
    private String sdsIDExtension; //done
    private String code; //done
    private String telecom; //done
    private String address; //done
    private String name; //done
    private String idRoot; //done
}

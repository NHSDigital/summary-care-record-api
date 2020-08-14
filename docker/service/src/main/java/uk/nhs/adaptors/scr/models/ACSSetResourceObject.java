package uk.nhs.adaptors.scr.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ACSSetResourceObject {
    private String function;
    private String resourceId;
    private String accessor;
    private String permission;
}

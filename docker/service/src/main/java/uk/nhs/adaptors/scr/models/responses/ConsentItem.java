package uk.nhs.adaptors.scr.models.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsentItem {
    private String permission;
    private String userData;
    private String resourceType;
    private String resourceId;
    private String function;
    private String functionCode;
    private String accessorType;
    private String accessorId;
    private String accessorName;
}

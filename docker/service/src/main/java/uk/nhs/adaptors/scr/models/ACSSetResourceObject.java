package uk.nhs.adaptors.scr.models;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter

public class ACSSetResourceObject {
    private String function;
    private String resourceId;
    private String accessor;
    private String permission;
}

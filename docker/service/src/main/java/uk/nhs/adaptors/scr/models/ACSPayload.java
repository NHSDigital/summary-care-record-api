package uk.nhs.adaptors.scr.models;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class ACSPayload {
    private List<ACSSetResourceObject> payload;
}

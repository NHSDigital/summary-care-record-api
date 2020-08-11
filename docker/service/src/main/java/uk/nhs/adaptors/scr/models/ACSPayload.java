package uk.nhs.adaptors.scr.models;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class ACSPayload {
    private ArrayList<ACSSetResourceObject> payload;
}

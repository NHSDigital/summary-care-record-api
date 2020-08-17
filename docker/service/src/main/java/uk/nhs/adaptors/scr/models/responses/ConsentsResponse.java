package uk.nhs.adaptors.scr.models.responses;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsentsResponse {
    private List<ConsentItem> consents;
}

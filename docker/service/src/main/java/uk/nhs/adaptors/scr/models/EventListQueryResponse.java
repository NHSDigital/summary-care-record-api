package uk.nhs.adaptors.scr.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventListQueryResponse {
    private AcsPermission viewPermission;
    private AcsPermission storePermission;
    private String latestScrId;
}

package uk.nhs.adaptors.scr.models;

import lombok.Getter;
import lombok.Setter;
/**
 * EventList is integral to the requests to get the SCR from Spine, specifically, getting the latest SCR ID
 * It's therefore used heavily in GetSCRService.
 */
@Getter
@Setter
public class EventListQueryResponse {
    private AcsPermission viewPermission;
    private AcsPermission storePermission;
    private String latestScrId;
}

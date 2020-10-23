package uk.nhs.adaptors.scr.models.gpsummarymodels;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObservationObject {
    private String observationId;
    private String observationCode;
    private String observationDisplay;
    private String observationStatus;
    private String observationStartTime;
    private List<EndTime> observationEndTimeList;
}

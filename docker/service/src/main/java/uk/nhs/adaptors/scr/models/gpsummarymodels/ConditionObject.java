package uk.nhs.adaptors.scr.models.gpsummarymodels;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConditionObject {
    private String conditionId;
    private String conditionCode;
    private String conditionDisplay;
    private String conditionStatus;
    private String conditionStartTime;
    private List<EndTime> conditionEndTimeList;
}

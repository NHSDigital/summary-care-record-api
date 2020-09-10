package uk.nhs.adaptors.scr.models.hl7models;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompositionObject {
    private String titleID; //done
    private String presentationTitle; //done
    private String presentationText; //done
    private String categoryDisplayName; //done
    private String categoryCodeSystem; //done
    private String categoryCode; //done
    private String priorMessageRef; //done
    private String replacementTypeCode; //done
    private String gpSummaryDisplayName; //done
    private String gpSummaryCodeSystem; //done
    private String gpSummaryID; //done
    private String title;
    private Date date;
}

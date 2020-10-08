package uk.nhs.adaptors.scr.models.gpsummarymodels;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationType {
    private List<OrganizationCode> organizationCodes;
}

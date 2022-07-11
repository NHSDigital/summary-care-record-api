package uk.nhs.adaptors.scr.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import uk.nhs.adaptors.scr.models.RoleEntry;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class PractitionerRoleResponse {
    private String resourceType;
    private String id;
    private List<RoleEntry> entry;
}


package uk.nhs.adaptors.scr.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class PractitionerRoleResponse {
    private String resourceType;
    private String id;
    private List<RoleEntry> entry;
}

@Builder
@Getter
@AllArgsConstructor
class RoleEntry {
    private String fullUrl;
    private RoleResource resource;
}

@Builder
@Getter
@AllArgsConstructor
class RoleResource {
    private String resourceType;
    private String id;
    private List<String> code;
}

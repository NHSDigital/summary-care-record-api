package uk.nhs.adaptors.scr.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class PractitionerRoleResponse {
    String ResourceType;
    String Id;
    List<RoleEntry> entry;
}

@Builder
@Getter
@AllArgsConstructor
class RoleEntry {
    String FullUrl;
    RoleResource Resource;
}

@Builder
@Getter
@AllArgsConstructor
class RoleResource {
    String ResourceType;
    String Id;
    List<String> Code;
}

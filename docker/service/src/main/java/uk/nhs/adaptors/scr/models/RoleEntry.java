package uk.nhs.adaptors.scr.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class RoleEntry {
    private String fullUrl;
    private RoleResource resource;
}

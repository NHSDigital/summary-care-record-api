package uk.nhs.adaptors.scr.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class RoleResource {
    private String resourceType;
    private String id;
    private List<String> code;
}

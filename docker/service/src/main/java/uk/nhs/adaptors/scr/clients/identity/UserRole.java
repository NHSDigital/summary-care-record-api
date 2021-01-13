package uk.nhs.adaptors.scr.clients.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class UserRole {
    @JsonProperty("activities")
    private List<String> activities;

    @JsonProperty("activity_codes")
    private List<String> activityCodes;

    @JsonProperty("org_code")
    private String orgCode;

    @JsonProperty("person_orgid")
    private String personOrgId;

    @JsonProperty("person_roleid")
    private String personRoleId;

    @JsonProperty("role_code")
    private String roleCode;

    @JsonProperty("role_name")
    private String roleName;
}

package uk.nhs.adaptors.scr.clients.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class UserInfo {
    @JsonProperty("name")
    private String name;

    @JsonProperty("nhsid_useruid")
    private String id;

    @JsonProperty("nhsid_nrbac_roles")
    private List<UserRole> roles;

    @JsonProperty("sub")
    private String sub;
}

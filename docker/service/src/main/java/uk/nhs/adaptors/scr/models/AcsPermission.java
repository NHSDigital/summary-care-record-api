package uk.nhs.adaptors.scr.models;

import lombok.Getter;

@Getter
public enum AcsPermission {
    YES("Yes", "Yes"),
    NO("No", "No"),
    ASK("Ask", "Clear");

    private String fhirValue;
    private String spineValue;

    AcsPermission(String value, String spineValue) {
        this.fhirValue = value;
        this.spineValue = spineValue;
    }

    public static AcsPermission fromValue(String str) {
        return valueOf(str.toUpperCase());
    }
}

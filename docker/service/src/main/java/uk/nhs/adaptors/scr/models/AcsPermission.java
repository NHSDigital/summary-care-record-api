package uk.nhs.adaptors.scr.models;

import lombok.Getter;

/**
 * Model for defining the permissions values in the $setPermission end point.
 */
@Getter
public enum AcsPermission {
    // Yes is legacy and therefore treated the same as ask.
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

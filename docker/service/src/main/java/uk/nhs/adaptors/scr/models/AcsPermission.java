package uk.nhs.adaptors.scr.models;

public enum AcsPermission {
    YES("Yes"),
    NO("No"),
    ASK("Ask");

    private String value;

    AcsPermission(String value) {
        this.value = value;
    }

    public static AcsPermission fromValue(String str) {
        return valueOf(str.toUpperCase());
    }

    public String value() {
        return value;
    }
}

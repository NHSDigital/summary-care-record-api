package uk.nhs.adaptors.scr.mappings.from.hl7;

public class PerformerParticipationMode {
    public static String getParticipationModeDisplay(String code) {
        switch (code) {
            case "ELECTRONIC":
                return "electronic data";
            case "PHYSICAL":
                return "physical presence";
            case "REMOTE":
                return "remote presence";
            case "VERBAL":
                return "verbal";
            case "DICTATE":
                return "dictated";
            case "FACE":
                return "face-to-face";
            case "PHONE":
                return "telephone";
            case "VIDEOCONF":
                return "videoconferencing";
            case "WRITTEN":
                return "written";
            case "FAXWRIT":
                return "telefax";
            case "HANDWRIT":
                return "handwritten";
            case "MAILWRIT":
                return "mail";
            case "ONLINEWRIT":
                return "online written";
            case "EMAILWRIT":
                return "email";
            case "TYPEWRIT":
                return "typewritten";
            default:
                return "";
        }
    }
}

package uk.nhs.adaptors.scr.controllers.utils;

public class UrlUtils {
    public static String extractBaseUrl(String clientRequestUrl, String requestUri) {
        int uriIndexOf = clientRequestUrl.indexOf(requestUri);
        return uriIndexOf >= 0 ? clientRequestUrl.substring(0, uriIndexOf) : clientRequestUrl;
    }
}

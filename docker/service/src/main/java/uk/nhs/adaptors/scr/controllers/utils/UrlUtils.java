package uk.nhs.adaptors.scr.controllers.utils;

import lombok.SneakyThrows;

import java.net.URL;

import static org.springframework.util.StringUtils.isEmpty;

public class UrlUtils {
    public static String extractBaseUrl(String clientRequestUrl, String requestUri) {
        int uriIndexOf = clientRequestUrl.indexOf(requestUri);
        return uriIndexOf >= 0 ? clientRequestUrl.substring(0, uriIndexOf) : clientRequestUrl;
    }

    @SneakyThrows
    public static String extractHost(String clientRequestUrl) {
        URL url = new URL(clientRequestUrl);
        String protocol = url.getProtocol();

        return isEmpty(protocol) ? url.getHost() : protocol + "://" + url.getHost();
    }
}

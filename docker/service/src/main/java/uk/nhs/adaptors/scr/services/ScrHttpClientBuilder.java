package uk.nhs.adaptors.scr.services;

import com.heroku.sdk.EnvKeyStore;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.config.SpineConfiguration;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.UUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ScrHttpClientBuilder {

    private final SpineConfiguration spineConfiguration;

    public CloseableHttpClient build() {
        var httpClientBuilder = HttpClients.custom();
        if (spineConfiguration.getUrl().startsWith("https://")) {
            LOGGER.debug("Setting up HTTP client with mutual TLS");
            //TODO: NoopHostnameVerifier works for mock - in production DefaultHostnameVerifier should be used
            NoopHostnameVerifier hostnameVerifier = new NoopHostnameVerifier();
            SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(buildSSLContext(), hostnameVerifier);
            httpClientBuilder.setSSLSocketFactory(factory);
        }
        return httpClientBuilder.build();
    }

    @SneakyThrows
    private SSLContext buildSSLContext() {
        var invalidSslValues = new ArrayList<String>();
        if (StringUtils.isBlank(spineConfiguration.getEndpointPrivateKey())) {
            invalidSslValues.add("private key");
        }
        if (StringUtils.isBlank(spineConfiguration.getEndpointCert())) {
            invalidSslValues.add("cert");
        }
        if (StringUtils.isBlank(spineConfiguration.getCaCerts())) {
            invalidSslValues.add("cacert");
        }
        if (!invalidSslValues.isEmpty()) {
            throw new ScrBaseException(String.format("Spine SSL %s %s not set",
                String.join(", ", invalidSslValues),
                invalidSslValues.size() == 1 ? "is" : "are"));
        }

        var randomPassword = UUID.randomUUID().toString();
        KeyStore ks = EnvKeyStore.createFromPEMStrings(
            spineConfiguration.getClientKey(),
            spineConfiguration.getClientCert(),
            randomPassword).keyStore();
        KeyStore ts = EnvKeyStore.createFromPEMStrings(
            spineConfiguration.getSubCA() + spineConfiguration.getRootCA(),
            randomPassword).keyStore();
        return SSLContexts.custom()
            .loadKeyMaterial(ks, randomPassword.toCharArray())
            .loadTrustMaterial(ts, TrustAllStrategy.INSTANCE)
            .build();
    }
}

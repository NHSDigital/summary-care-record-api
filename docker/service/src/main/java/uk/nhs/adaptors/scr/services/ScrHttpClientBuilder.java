package uk.nhs.adaptors.scr.services;

import com.heroku.sdk.EnvKeyStore;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.config.SpineConfiguration;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
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
        var randomPassword = UUID.randomUUID().toString();
        KeyStore ks = EnvKeyStore.createFromPEMStrings(
            spineConfiguration.getEndpointPrivateKey(),
            spineConfiguration.getEndpointCert(),
            randomPassword).keyStore();
        KeyStore ts = EnvKeyStore.createFromPEMStrings(
            spineConfiguration.getCaCerts(),
            randomPassword).keyStore();
        return SSLContexts.custom()
            .loadKeyMaterial(ks, randomPassword.toCharArray())
            .loadTrustMaterial(ts, TrustAllStrategy.INSTANCE)
            .build();
    }
}
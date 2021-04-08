package uk.nhs.adaptors.scr.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HeaderIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import uk.nhs.adaptors.scr.exceptions.ScrBaseException;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.UUID;

import static com.heroku.sdk.EnvKeyStore.createFromPEMStrings;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.http.conn.ssl.TrustAllStrategy.INSTANCE;
import static org.apache.http.protocol.HTTP.CONN_KEEP_ALIVE;

@Configuration
@EnableScheduling
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ApacheHttpClientConfig {

    private static final int CLOSE_IDLE_CONNECTIONS_INTERVAL = 20000;
    private static final int TASK_SCHEDULER_POOL_SIZE = 5;

    private final SpineConfiguration spineConfiguration;
    private final SpineConnectionPoolConfig connectionPoolConfig;

    @Bean
    public PoolingHttpClientConnectionManager poolingConnectionManager() {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = getConnectionSocketFactoryRegistry();

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setMaxTotal(connectionPoolConfig.getMaxTotalConnections());
        poolingConnectionManager.setDefaultMaxPerRoute(connectionPoolConfig.getMaxTotalConnections());
        return poolingConnectionManager;
    }

    private Registry<ConnectionSocketFactory> getConnectionSocketFactoryRegistry() {
        RegistryBuilder<ConnectionSocketFactory> builder = RegistryBuilder.create();
        if (spineConfiguration.isTlsEnabled()) {
            builder.register("https", new SSLConnectionSocketFactory(buildSSLContext(spineConfiguration)));
        } else {
            builder.register("http", PlainConnectionSocketFactory.getSocketFactory());
        }
        return builder.build();
    }

    @Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (httpResponse, httpContext) -> {
            HeaderIterator headerIterator = httpResponse.headerIterator(CONN_KEEP_ALIVE);
            HeaderElementIterator elementIterator = new BasicHeaderElementIterator(headerIterator);

            while (elementIterator.hasNext()) {
                HeaderElement element = elementIterator.nextElement();
                String param = element.getName();
                String value = element.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return MILLISECONDS.toSeconds(Long.parseLong(value));
                }
            }

            return connectionPoolConfig.getDefaultKeepAliveTime();
        };
    }

    @Bean
    public Runnable idleConnectionMonitor(PoolingHttpClientConnectionManager pool) {
        return new Runnable() {
            @Override
            @Scheduled(fixedDelay = CLOSE_IDLE_CONNECTIONS_INTERVAL)
            public void run() {
                if (pool != null) {
                    pool.closeExpiredConnections();
                    pool.closeIdleConnections(connectionPoolConfig.getIdleConnectionWaitTime(), MILLISECONDS);
                }
            }
        };
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("idleMonitor");
        scheduler.setPoolSize(TASK_SCHEDULER_POOL_SIZE);
        return scheduler;
    }

    @Bean
    @Autowired
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager poolingManager,
                                          ConnectionKeepAliveStrategy keepAliveStrategy) {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(connectionPoolConfig.getConnectionTimeout())
            .setConnectionRequestTimeout(connectionPoolConfig.getRequestTimeout())
            .setSocketTimeout(connectionPoolConfig.getSocketTimeout())
            .build();

        var httpClientBuilder = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .setConnectionManager(poolingManager)
            .setKeepAliveStrategy(keepAliveStrategy);

        return httpClientBuilder.build();
    }

    @SneakyThrows
    private SSLContext buildSSLContext(SpineConfiguration spineConfiguration) {
        var invalidSslValues = new ArrayList<String>();
        if (StringUtils.isBlank(spineConfiguration.getClientKey())) {
            invalidSslValues.add("private key");
        }
        if (StringUtils.isBlank(spineConfiguration.getClientCert())) {
            invalidSslValues.add("cert");
        }
        if (StringUtils.isBlank(spineConfiguration.getSubCA() + spineConfiguration.getRootCA())) {
            invalidSslValues.add("cacert");
        }
        if (!invalidSslValues.isEmpty()) {
            throw new ScrBaseException(String.format("Spine SSL %s %s not set",
                String.join(", ", invalidSslValues),
                invalidSslValues.size() == 1 ? "is" : "are"));
        }

        var randomPassword = UUID.randomUUID().toString();
        KeyStore ks = createFromPEMStrings(
            spineConfiguration.getClientKey(),
            spineConfiguration.getClientCert(),
            randomPassword).keyStore();
        KeyStore ts = createFromPEMStrings(
            spineConfiguration.getSubCA() + spineConfiguration.getRootCA(),
            randomPassword).keyStore();
        return SSLContexts.custom()
            .loadKeyMaterial(ks, randomPassword.toCharArray())
            .loadTrustMaterial(ts, INSTANCE)
            .build();
    }
}

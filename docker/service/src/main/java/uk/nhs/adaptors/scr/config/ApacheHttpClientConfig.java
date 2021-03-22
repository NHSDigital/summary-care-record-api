package uk.nhs.adaptors.scr.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
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
            builder.register("https", new SSLConnectionSocketFactory(SSLContexts.createSystemDefault()));
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

}

package com.test.app.widget.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Setter
@Getter
@ConfigurationProperties(prefix = "widget.config")
@Component
public class ConfigProperties {

    private StorageH2 h2storage;
    private PagingConfig pagingConfig;
    private RateLimiting rateLimiting;

    @Getter
    @Setter
    public static class StorageH2 {
        private boolean enabled;
    }

    @Getter
    @Setter
    public static class PagingConfig {
        private int widgetLimitForLoadingPerQuery;
        private int defaultPagingResultSize;
    }

    @Getter
    @Setter
    public static class RateLimiting {
        private int limit;
        private LocalDateTime nextResetDateTime = LocalDateTime.now();
        private int availableRequests;

        public void decrementAvailableRequests() {
            this.availableRequests -= 1;
        }
    }
}

package com.example.mqtt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private InsertProperties insert = new InsertProperties();
    private RetentionProperties retention = new RetentionProperties();
    private MqttProperties mqtt = new MqttProperties();

    @Data
    public static class InsertProperties {
        private int batchSize = 100;
        private long batchIntervalMs = 500L;
    }

    @Data
    public static class RetentionProperties {
        private int days = 30;
    }

    @Data
    public static class MqttProperties {
        private boolean enabled = true;
        private String serverUri;
        private String clientId;
        private String username;
        private String password;
        private int connectionTimeoutSeconds = 10;
        private int keepAliveSeconds = 30;
        private boolean cleanSession = true;
        private int reconnectDelaySeconds = 5;
    }
}



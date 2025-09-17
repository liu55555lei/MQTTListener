package com.example.mqtt.health;

import com.example.mqtt.config.AppProperties;
import com.example.mqtt.service.MqttClientService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MqttHealthIndicator implements HealthIndicator {

    private final MqttClientService mqttClientService;
    private final AppProperties props;

    public MqttHealthIndicator(MqttClientService mqttClientService, AppProperties props) {
        this.mqttClientService = mqttClientService;
        this.props = props;
    }

    @Override
    public Health health() {
        boolean up = mqttClientService != null && mqttClientService.isRunning();
        return up ? Health.up().withDetail("server", props.getMqtt().getServerUri()).build()
                : Health.down().withDetail("server", props.getMqtt().getServerUri()).build();
    }
}



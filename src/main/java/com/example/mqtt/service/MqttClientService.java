package com.example.mqtt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mqtt.config.AppProperties;
import com.example.mqtt.entity.MqttTopic;
import com.example.mqtt.mapper.MqttTopicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.context.SmartLifecycle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class MqttClientService implements SmartLifecycle {

    private final AppProperties properties;
    private final MqttTopicMapper topicMapper;
    private final MessageBatchWriter batchWriter;
    private final ConfigService configService;
    private final @Qualifier("mqttExecutor") Executor mqttExecutor;

    private volatile boolean running = false;
    private MqttAsyncClient client;
    private final Set<String> subscribed = new HashSet<>();

    @Override
    public void start() {
        connectAndSubscribe();
        running = true;
    }

    private void connectAndSubscribe() {
        try {
            if (client != null && client.isConnected()) return;
            if (properties.getMqtt().getServerUri() == null || properties.getMqtt().getServerUri().isBlank()) {
                log.info("MQTT server URI is empty, skip connect until DB config applied.");
                return;
            }

            client = new MqttAsyncClient(properties.getMqtt().getServerUri(), properties.getMqtt().getClientId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(properties.getMqtt().isCleanSession());
            options.setConnectionTimeout(properties.getMqtt().getConnectionTimeoutSeconds());
            options.setKeepAliveInterval(properties.getMqtt().getKeepAliveSeconds());
            if (properties.getMqtt().getUsername() != null) {
                options.setUserName(properties.getMqtt().getUsername());
            }
            if (properties.getMqtt().getPassword() != null) {
                options.setPassword(properties.getMqtt().getPassword().toCharArray());
            }

            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    log.info("MQTT connect complete. reconnect={}, uri={} ", reconnect, serverURI);
                    refreshSubscriptions();
                }

                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    mqttExecutor.execute(() -> handleMessage(topic, message));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) { }
            });

            client.connect(options).waitForCompletion();
        } catch (Exception e) {
            log.error("MQTT connect failed", e);
        }
    }

    private void handleMessage(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        if (!configService.allowPayload(payload)) {
            return;
        }
        boolean valid = JsonUtils.isValidJson(payload);
        String payloadToStore = valid ? payload : ("{\"raw\":" + escapeForJson(payload) + "}");
        
        // 获取topic的标识位路径
        String identifierPath = getIdentifierPathForTopic(topic);
        
        // Per requirement: always store every received message; do not set messageId to avoid unique conflicts
        batchWriter.enqueue(topic, null, payloadToStore, valid, identifierPath);
    }
    
    private String getIdentifierPathForTopic(String topic) {
        try {
            List<MqttTopic> topics = topicMapper.selectList(
                new LambdaQueryWrapper<MqttTopic>()
                    .eq(MqttTopic::getTopicName, topic)
                    .eq(MqttTopic::getIsEnabled, 1)
            );
            if (!topics.isEmpty()) {
                return topics.get(0).getIdentifierPath();
            }
        } catch (Exception e) {
            log.warn("Failed to get identifier path for topic {}: {}", topic, e.getMessage());
        }
        return null;
    }

    private String escapeForJson(String s) {
        return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

    @Scheduled(fixedDelay = 5000)
    public void ensureConnected() {
        if (client == null || !client.isConnected()) {
            connectAndSubscribe();
        }
    }

    public synchronized void refreshSubscriptions() {
        try {
            if (client == null || !client.isConnected()) return;
            List<MqttTopic> list = topicMapper.selectList(new LambdaQueryWrapper<MqttTopic>().eq(MqttTopic::getIsEnabled, 1));
            Set<String> wanted = new HashSet<>();
            for (MqttTopic t : list) {
                wanted.add(t.getTopicName());
            }
            // unsubscribe removed
            for (String current : new HashSet<>(subscribed)) {
                if (!wanted.contains(current)) {
                    client.unsubscribe(current);
                    subscribed.remove(current);
                    log.info("Unsubscribed topic {}", current);
                }
            }
            // subscribe new
            for (String t : wanted) {
                if (!subscribed.contains(t)) {
                    client.subscribe(t, 1);
                    subscribed.add(t);
                    log.info("Subscribed topic {}", t);
                }
            }
        } catch (Exception e) {
            log.error("Refresh subscriptions failed", e);
        }
    }

    @Override
    public void stop() {
        running = false;
        try {
            if (client != null) client.disconnectForcibly(1000);
        } catch (Exception ignored) {}
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}



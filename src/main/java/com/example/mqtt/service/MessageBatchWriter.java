package com.example.mqtt.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.mqtt.config.AppProperties;
import com.example.mqtt.entity.MqttMessage;
import com.example.mqtt.mapper.MqttMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBatchWriter {

    private final MqttMessageMapper messageMapper;
    private final AppProperties properties;

    private final ConcurrentLinkedQueue<MqttMessage> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(String topic, String messageId, String payloadJson, boolean valid) {
        enqueue(topic, messageId, payloadJson, valid, null);
    }

    public void enqueue(String topic, String messageId, String payloadJson, boolean valid, String identifierPath) {
        MqttMessage msg = new MqttMessage();
        msg.setChannelName(topic);
        msg.setMessageId(messageId);
        msg.setDataContent(payloadJson);
        msg.setReceiveTime(LocalDateTime.now());
        msg.setStatus(valid ? 1 : 0);

        // 提取标识位值
        if (identifierPath != null && !identifierPath.trim().isEmpty() && valid) {
            try {
                String identifierValue = JsonUtils.extractJsonPath(payloadJson, identifierPath);
                msg.setIdentifierValue(identifierValue);
            } catch (Exception e) {
                log.warn("Failed to extract identifier from path {}: {}", identifierPath, e.getMessage());
            }
        }
        //log.info("MSG {}:{}", topic, msg.getIdentifierValue());
        queue.offer(msg);
    }

    @Scheduled(fixedDelayString = "${app.insert.batch-interval-ms:500}")
    public void flush() {
        int batchSize = Math.max(1, properties.getInsert().getBatchSize());
        List<MqttMessage> buffer = new ArrayList<>(batchSize);
        while (buffer.size() < batchSize) {
            MqttMessage m = queue.poll();
            if (m == null) break;
            buffer.add(m);
        }
        if (buffer.isEmpty()) return;

        try {
            log.debug("Flushing {} messages", buffer.size());
            for (MqttMessage m : buffer) {
                int r = messageMapper.insert(m);
                log.debug("Inserted message rows={}, topic={}, time={}", r, m.getChannelName(), m.getReceiveTime());
            }
        } catch (Exception e) {
            log.error("Batch insert failed, re-queueing {} messages", buffer.size(), e);
            // put back for retry
            buffer.forEach(queue::offer);
        }
    }
}



package com.example.mqtt.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.mqtt.config.AppProperties;
import com.example.mqtt.entity.MqttMessage;
import com.example.mqtt.entity.MqttMessageDetail;
import com.example.mqtt.mapper.MqttMessageMapper;
import com.example.mqtt.mapper.MqttMessageDetailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBatchWriter {

    private final MqttMessageMapper messageMapper;
    private final MqttMessageDetailMapper messageDetailMapper;
    private final AppProperties properties;

    private final ConcurrentLinkedQueue<MessageData> queue = new ConcurrentLinkedQueue<>();
    
    /**
     * 消息数据结构，包含主表信息和明细信息
     */
    private static class MessageData {
        MqttMessage message;
        String jsonContent;
    }
    public void enqueue(String topic, String payloadJson, boolean valid, String identifierPath) {
        MqttMessage msg = new MqttMessage();
        msg.setChannelName(topic);
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
        
        // 创建消息数据对象
        MessageData messageData = new MessageData();
        messageData.message = msg;
        messageData.jsonContent = payloadJson;
        
        //log.info("MSG {}:{}", topic, msg.getIdentifierValue());
        queue.offer(messageData);
    }

    @Scheduled(fixedDelayString = "${app.insert.batch-interval-ms:500}")
    public void flush() {
        int batchSize = Math.max(1, properties.getInsert().getBatchSize());
        List<MessageData> buffer = new ArrayList<>(batchSize);
        
        // 优化：收集更多消息以提高批量效率
        while (buffer.size() < batchSize) {
            MessageData m = queue.poll();
            if (m == null) break;
            buffer.add(m);
        }
        
        if (buffer.isEmpty()) return;

        try {
            log.debug("Flushing {} messages", buffer.size());
            // 优化：使用批量插入提高性能
            batchInsertMessages(buffer);
        } catch (Exception e) {
            log.error("Batch insert failed, re-queueing {} messages", buffer.size(), e);
            // put back for retry
            buffer.forEach(queue::offer);
        }
    }
    
    /**
     * 批量插入消息，提高性能
     * 使用真正的批量插入，大幅提升性能
     */
    @Transactional
    public void batchInsertMessages(List<MessageData> messageDataList) {
        if (messageDataList.isEmpty()) return;
        
        // 准备主表数据
        List<MqttMessage> messages = new ArrayList<>();
        for (MessageData messageData : messageDataList) {
            messages.add(messageData.message);
        }
        int messageRows = messageMapper.insertBatch(messages);

        // 准备明细数据(主表插入数据后才有id)
        List<MqttMessageDetail> details = new ArrayList<>();
        for (MessageData messageData : messageDataList) {
            MqttMessageDetail detail = new MqttMessageDetail();
            detail.setId(messageData.message.getId());
            detail.setJsonContent(messageData.jsonContent);
            details.add(detail);
        }
        int detailRows = messageDetailMapper.insertBatch(details);
        
        log.debug("Batch inserted: messages={}, details={}, total={}, messageRows={}, detailRows={}", 
                 messages.size(), details.size(), messageDataList.size(), messageRows, detailRows);
    }
}



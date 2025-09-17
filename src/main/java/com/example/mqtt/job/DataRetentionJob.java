package com.example.mqtt.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mqtt.config.AppProperties;
import com.example.mqtt.entity.MqttMessage;
import com.example.mqtt.mapper.MqttMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataRetentionJob {
    private final MqttMessageMapper messageMapper;
    private final AppProperties properties;

    // Run daily at 02:30
    @Scheduled(cron = "0 30 2 * * ?")
    public void cleanOld() {
        int days = properties.getRetention().getDays();
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        int before = messageMapper.selectCount(null).intValue();
        int deleted = messageMapper.delete(new LambdaQueryWrapper<MqttMessage>().lt(MqttMessage::getReceiveTime, threshold));
        log.info("Retention cleanup: days={}, deleted={}, before={}", days, deleted, before);
    }
}



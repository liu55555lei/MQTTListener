package com.example.mqtt.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mqtt.config.AppProperties;
import com.example.mqtt.entity.MqttMessage;
import com.example.mqtt.entity.MqttMessageDetail;
import com.example.mqtt.mapper.MqttMessageDetailMapper;
import com.example.mqtt.mapper.MqttMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataRetentionJob {
    private final MqttMessageMapper messageMapper;
    private final MqttMessageDetailMapper messageDetailMapper;
    private final AppProperties properties;

    // 消息明细清理：每天凌晨2:30执行
    @Scheduled(cron = "0 30 2 * * ?")
    @Async
    public void cleanMessageDetails() {
        log.info("开始执行消息明细清理任务");
        try {
            int messageDetailDays = properties.getRetention().getMessageDetailDays();
            LocalDateTime threshold = LocalDateTime.now().minusDays(messageDetailDays);
            
            // 分批处理，避免一次性加载过多数据
            int batchSize = 1000;
            int totalDeleted = 0;
            int before = messageDetailMapper.selectCount(null).intValue();
            
            while (true) {
                // 分批查询需要删除的消息ID
                LambdaQueryWrapper<MqttMessage> queryWrapper = new LambdaQueryWrapper<MqttMessage>()
                        .lt(MqttMessage::getReceiveTime, threshold)
                        .select(MqttMessage::getId)
                        .last("LIMIT " + batchSize);
                
                List<Object> messageIds = messageMapper.selectObjs(queryWrapper);
                
                if (messageIds.isEmpty()) {
                    break; // 没有更多数据需要清理
                }
                
                // 批量删除消息明细
                int deleted = messageDetailMapper.deleteBatchIds(messageIds);
                totalDeleted += deleted;
                
                log.debug("消息明细清理批次: 删除记录数={}, 累计删除={}", deleted, totalDeleted);
                
                // 如果删除的记录数少于批次大小，说明已经处理完所有数据
                if (deleted < batchSize) {
                    break;
                }
            }
            
            log.info("消息明细清理完成: 保留天数={}, 总删除记录数={}, 清理前总数={}", 
                    messageDetailDays, totalDeleted, before);
                    
        } catch (Exception e) {
            log.error("消息明细清理任务执行失败", e);
        }
    }
    
    // 消息主表清理：每天凌晨2:35执行（延迟5分钟，避免与明细清理冲突）
    @Scheduled(cron = "0 35 2 * * ?")
    @Async
    public void cleanMessages() {
        log.info("开始执行消息主表清理任务");
        try {
            int messageDays = properties.getRetention().getMessageDays();
            LocalDateTime threshold = LocalDateTime.now().minusDays(messageDays);
            
            int before = messageMapper.selectCount(null).intValue();
            
            // 优化：使用更大的批次大小，减少数据库交互次数
            int batchSize = 5000; // 增加批次大小
            int totalDeleted = 0;
            int maxBatches = 100; // 限制最大批次数，避免长时间运行
            int batchCount = 0;
            
            while (batchCount < maxBatches) {
                // 优化：先查询要删除的ID，然后批量删除
                LambdaQueryWrapper<MqttMessage> queryWrapper = new LambdaQueryWrapper<MqttMessage>()
                        .lt(MqttMessage::getReceiveTime, threshold)
                        .select(MqttMessage::getId) // 只查询ID，减少数据传输
                        .last("LIMIT " + batchSize);
                
                List<Object> idsToDelete = messageMapper.selectObjs(queryWrapper);
                
                if (idsToDelete.isEmpty()) {
                    break; // 没有更多数据需要清理
                }
                
                // 批量删除
                int deleted = messageMapper.deleteBatchIds(idsToDelete);
                totalDeleted += deleted;
                batchCount++;
                
                log.debug("消息主表清理批次: 删除记录数={}, 累计删除={}, 批次={}", 
                         deleted, totalDeleted, batchCount);
                
                // 如果删除的记录数少于批次大小，说明已经处理完所有数据
                if (deleted < batchSize) {
                    break;
                }
                
                // 添加短暂延迟，避免对数据库造成过大压力
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            log.info("消息主表清理完成: 保留天数={}, 总删除记录数={}, 清理前总数={}, 处理批次数={}", 
                    messageDays, totalDeleted, before, batchCount);
                    
        } catch (Exception e) {
            log.error("消息主表清理任务执行失败", e);
        }
    }
}



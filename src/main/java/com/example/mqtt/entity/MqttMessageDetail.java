package com.example.mqtt.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * MQTT消息明细信息表
 * 存储消息的JSON内容，与mqtt_message表通过id关联
 */
@Data
@TableName("mqtt_message_detail")
public class MqttMessageDetail {
    @TableId
    private Long id; // 与mqtt_message表的id保持一致，使用雪花算法生成
    private String jsonContent; // 消息的JSON内容
}

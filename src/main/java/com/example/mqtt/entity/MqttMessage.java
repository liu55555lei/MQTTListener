package com.example.mqtt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQTT消息主表
 * 存储消息的基本信息，不包含JSON内容
 * 使用雪花算法生成ID
 */
@Data
@TableName("mqtt_message")
public class MqttMessage {
    @TableId(type = IdType.ASSIGN_ID) // 使用雪花算法生成ID
    private Long id;
    private String channelName;
    private String identifierValue; // 标识位值
    private LocalDateTime receiveTime;
    private Integer status;
}



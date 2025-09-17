package com.example.mqtt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mqtt_message")
public class MqttMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String channelName;
    private String dataContent; // store JSON string, MySQL JSON type supported
    private String identifierValue; // 标识位值
    private LocalDateTime receiveTime;
    private String messageId;
    private Integer status;
}



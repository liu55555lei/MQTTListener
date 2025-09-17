package com.example.mqtt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mqtt_topic")
public class MqttTopic {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String topicName;
    private String identifierPath;
    private Integer isEnabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}



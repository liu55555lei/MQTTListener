package com.example.mqtt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_config")
public class SystemConfig {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String configKey;
    private String configValue;
    private String configDesc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}



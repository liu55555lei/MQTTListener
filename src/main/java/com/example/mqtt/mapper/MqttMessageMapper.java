package com.example.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mqtt.entity.MqttMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MqttMessageMapper extends BaseMapper<MqttMessage> {
}



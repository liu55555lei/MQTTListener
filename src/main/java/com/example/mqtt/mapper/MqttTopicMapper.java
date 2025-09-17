package com.example.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mqtt.entity.MqttTopic;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MqttTopicMapper extends BaseMapper<MqttTopic> {
}



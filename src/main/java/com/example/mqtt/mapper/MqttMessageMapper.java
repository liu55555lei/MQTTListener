package com.example.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mqtt.entity.MqttMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MqttMessageMapper extends BaseMapper<MqttMessage> {
    
    /**
     * 批量插入消息
     * 使用原生SQL实现真正的批量插入，性能远优于逐条插入
     */
    @Insert("<script>" +
            "INSERT INTO mqtt_message (id, channel_name, identifier_value, receive_time, status) VALUES " +
            "<foreach collection='messages' item='msg' separator=','>" +
            "(#{msg.id}, #{msg.channelName}, #{msg.identifierValue}, #{msg.receiveTime}, #{msg.status})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("messages") List<MqttMessage> messages);
}



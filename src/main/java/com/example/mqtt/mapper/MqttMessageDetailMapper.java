package com.example.mqtt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mqtt.entity.MqttMessageDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MQTT消息明细信息表Mapper接口
 */
@Mapper
public interface MqttMessageDetailMapper extends BaseMapper<MqttMessageDetail> {
    
    /**
     * 批量插入消息明细
     * 使用原生SQL实现真正的批量插入，性能远优于逐条插入
     */
    @Insert("<script>" +
            "INSERT INTO mqtt_message_detail (id, json_content) VALUES " +
            "<foreach collection='details' item='detail' separator=','>" +
            "(#{detail.id}, #{detail.jsonContent})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("details") List<MqttMessageDetail> details);
}

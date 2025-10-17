package com.example.mqtt.mapper;

import com.example.mqtt.entity.MqttMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MessageQueryMapper {

    @Select({
            "<script>",
            "SELECT id, channel_name AS channelName, identifier_value AS identifierValue, receive_time AS receiveTime, status",
            "FROM (",
            "  SELECT t.*, ROW_NUMBER() OVER (PARTITION BY t.identifier_value ORDER BY t.receive_time DESC, t.id DESC) rn",
            "  FROM mqtt_message t",
            "  WHERE t.status = 1",
            "  <if test='channelName != null and channelName != \"\"'>",
            "    AND t.channel_name LIKE CONCAT('%', #{channelName}, '%')",
            "  </if>",
            "  <if test='startTime != null'>",
            "    AND t.receive_time &gt;= #{startTime}",
            "  </if>",
            "  <if test='endTime != null'>",
            "    AND t.receive_time &lt;= #{endTime}",
            "  </if>",
            "  <if test='identifiers != null and identifiers.size() &gt; 0'>",
            "    AND t.identifier_value IN",
            "    <foreach collection='identifiers' item='it' open='(' separator=',' close=')'>",
            "      #{it}",
            "    </foreach>",
            "  </if>",
            ") x",
            "WHERE x.rn = 1",
            "ORDER BY x.receive_time DESC, x.id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<MqttMessage> selectLatestPerIdentifier(
            @Param("channelName") String channelName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("identifiers") List<String> identifiers,
            @Param("offset") long offset,
            @Param("limit") long limit
    );

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM (",
            "  SELECT 1",
            "  FROM mqtt_message t",
            "  WHERE t.status = 1",
            "  <if test='channelName != null and channelName != \"\"'>",
            "    AND t.channel_name LIKE CONCAT('%', #{channelName}, '%')",
            "  </if>",
            "  <if test='startTime != null'>",
            "    AND t.receive_time &gt;= #{startTime}",
            "  </if>",
            "  <if test='endTime != null'>",
            "    AND t.receive_time &lt;= #{endTime}",
            "  </if>",
            "  <if test='identifiers != null and identifiers.size() &gt; 0'>",
            "    AND t.identifier_value IN",
            "    <foreach collection='identifiers' item='it' open='(' separator=',' close=')'>",
            "      #{it}",
            "    </foreach>",
            "  </if>",
            "  GROUP BY t.identifier_value",
            ") c",
            "</script>"
    })
    long countDistinctIdentifiers(
            @Param("channelName") String channelName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("identifiers") List<String> identifiers
    );
}



package com.example.mqtt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mqtt.entity.MqttMessage;
import com.example.mqtt.mapper.MqttMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {
    private final MqttMessageMapper messageMapper;

    @GetMapping("/query")
    public ApiResponse<Object> query(
            @RequestParam(value = "channelName", required = false) String channelName,
            @RequestParam(value = "startTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(value = "identifierValues", required = false) String identifierValues,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize
    ) {
        LambdaQueryWrapper<MqttMessage> qw = new LambdaQueryWrapper<>();
        if (channelName != null && !channelName.isEmpty()) {
            qw.eq(MqttMessage::getChannelName, channelName);
        }
        if (startTime != null) {
            qw.ge(MqttMessage::getReceiveTime, startTime);
        }
        if (endTime != null) {
            qw.le(MqttMessage::getReceiveTime, endTime);
        }
        if (identifierValues != null && !identifierValues.isEmpty()) {
            // 支持多个标识值，用逗号分隔，匹配任意一个
            String[] values = identifierValues.split(",");
            if (values.length == 1) {
                qw.eq(MqttMessage::getIdentifierValue, values[0].trim());
            } else {
                qw.in(MqttMessage::getIdentifierValue, 
                    java.util.Arrays.stream(values)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(java.util.stream.Collectors.toList()));
            }
        }
        qw.orderByDesc(MqttMessage::getReceiveTime);
        Page<MqttMessage> page = messageMapper.selectPage(Page.of(pageNum, pageSize), qw);
        return ApiResponse.ok(page);
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<MqttMessage> detail(@PathVariable("id") Long id) {
        MqttMessage message = messageMapper.selectById(id);
        return ApiResponse.ok(message);
    }

    @GetMapping("/export")
    public void export(
            jakarta.servlet.http.HttpServletResponse response,
            @RequestParam(required = false) String channelName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) throws Exception {
        LambdaQueryWrapper<MqttMessage> qw = new LambdaQueryWrapper<>();
        if (channelName != null && !channelName.isEmpty()) qw.like(MqttMessage::getChannelName, channelName);
        if (startTime != null) qw.ge(MqttMessage::getReceiveTime, startTime);
        if (endTime != null) qw.le(MqttMessage::getReceiveTime, endTime);
        qw.orderByDesc(MqttMessage::getReceiveTime);
        List<MqttMessage> list = messageMapper.selectList(qw);

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=messages.csv");
        String header = "channelName,dataContent,receiveTime,status\n";
        response.getWriter().write(header);
        for (MqttMessage m : list) {
            String line = String.format("%s,\"%s\",%s,%d\n",
                    safe(m.getChannelName()), m.getDataContent().replace("\"", "'"),
                    m.getReceiveTime() == null ? "" : m.getReceiveTime().toString(), m.getStatus());
            response.getWriter().write(line);
        }
        response.getWriter().flush();
    }

    private String safe(String s){ return s==null?"":s.replace(","," "); }
}



package com.example.mqtt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.mqtt.entity.MqttTopic;
import com.example.mqtt.mapper.MqttTopicMapper;
import com.example.mqtt.service.MqttClientService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mqtt/topic")
@RequiredArgsConstructor
@Validated
public class TopicController {
    private final MqttTopicMapper topicMapper;
    private final MqttClientService mqttClientService;

    @PostMapping("/add")
    @Transactional
    public ApiResponse<Void> add(@RequestParam("topicName") @NotBlank String topicName,
                                @RequestParam(value = "identifierPath", required = false) String identifierPath) {
        MqttTopic t = new MqttTopic();
        t.setTopicName(topicName);
        t.setIdentifierPath(identifierPath);
        t.setIsEnabled(1);
        topicMapper.insert(t);
        mqttClientService.refreshSubscriptions();
        return ApiResponse.ok();
    }

    @PostMapping("/toggle")
    @Transactional
    public ApiResponse<Void> toggle(@RequestParam("id") Integer id, @RequestParam("enabled") Integer enabled) {
        topicMapper.update(null, new LambdaUpdateWrapper<MqttTopic>().eq(MqttTopic::getId, id).set(MqttTopic::getIsEnabled, enabled));
        mqttClientService.refreshSubscriptions();
        return ApiResponse.ok();
    }

    @PostMapping("/delete")
    @Transactional
    public ApiResponse<Void> delete(@RequestParam("id") Integer id) {
        topicMapper.deleteById(id);
        mqttClientService.refreshSubscriptions();
        return ApiResponse.ok();
    }

    @GetMapping("/list")
    public ApiResponse<List<MqttTopic>> list() {
        return ApiResponse.ok(topicMapper.selectList(new LambdaQueryWrapper<>()));
    }
}



package com.example.mqtt.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.mqtt.entity.SystemConfig;
import com.example.mqtt.mapper.SystemConfigMapper;
import com.example.mqtt.service.MqttClientService;
import com.example.mqtt.service.ConfigService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mqtt/config")
@RequiredArgsConstructor
@Validated
public class ConfigController {

    private final SystemConfigMapper configMapper;
    private final MqttClientService mqttClientService;
    private final ConfigService configService;

    @PostMapping("/save")
    @Transactional
    public ApiResponse<Void> save(@RequestBody Map<String, String> cfg) {
        cfg.forEach((k, v) -> upsert(k, v));
        configService.reloadAndApply();
        mqttClientService.ensureConnected();
        return ApiResponse.ok();
    }

    private void upsert(String key, String value) {
        Long count = configMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        if (count != null && count > 0) {
            configMapper.update(null, new LambdaUpdateWrapper<SystemConfig>()
                    .eq(SystemConfig::getConfigKey, key)
                    .set(SystemConfig::getConfigValue, value));
        } else {
            SystemConfig sc = new SystemConfig();
            sc.setConfigKey(key);
            sc.setConfigValue(value);
            configMapper.insert(sc);
        }
    }

    @GetMapping("/list")
    public ApiResponse<Object> list() {
        return ApiResponse.ok(configMapper.selectList(null));
    }
}



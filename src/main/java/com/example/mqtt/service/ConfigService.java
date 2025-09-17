package com.example.mqtt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mqtt.config.AppProperties;
import com.example.mqtt.entity.SystemConfig;
import com.example.mqtt.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigService {
    private final SystemConfigMapper configMapper;
    private final AppProperties appProperties;

    private volatile Set<String> includeKeywords = Collections.emptySet();
    private volatile Set<String> excludeKeywords = Collections.emptySet();

    public void reloadAndApply() {
        List<SystemConfig> list = configMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, String> map = new HashMap<>();
        for (SystemConfig c : list) {
            map.put(c.getConfigKey(), Optional.ofNullable(c.getConfigValue()).orElse(""));
        }
        // Apply MQTT configs if present
        opt(map, "app.mqtt.enabled").ifPresent(v -> appProperties.getMqtt().setEnabled(Boolean.parseBoolean(v)));
        opt(map, "app.mqtt.server-uri").ifPresent(v -> appProperties.getMqtt().setServerUri(v));
        opt(map, "app.mqtt.client-id").ifPresent(v -> appProperties.getMqtt().setClientId(v));
        opt(map, "app.mqtt.username").ifPresent(v -> appProperties.getMqtt().setUsername(v));
        opt(map, "app.mqtt.password").ifPresent(v -> appProperties.getMqtt().setPassword(v));
        opt(map, "app.mqtt.connection-timeout-seconds").ifPresent(v -> appProperties.getMqtt().setConnectionTimeoutSeconds(Integer.parseInt(v)));
        opt(map, "app.mqtt.keep-alive-seconds").ifPresent(v -> appProperties.getMqtt().setKeepAliveSeconds(Integer.parseInt(v)));
        opt(map, "app.insert.batch-size").ifPresent(v -> appProperties.getInsert().setBatchSize(Integer.parseInt(v)));
        opt(map, "app.insert.batch-interval-ms").ifPresent(v -> appProperties.getInsert().setBatchIntervalMs(Long.parseLong(v)));
        opt(map, "app.retention.days").ifPresent(v -> appProperties.getRetention().setDays(Integer.parseInt(v)));

        includeKeywords = parseCsv(opt(map, "app.filter.include").orElse(""));
        excludeKeywords = parseCsv(opt(map, "app.filter.exclude").orElse(""));
        log.info("Config applied. includeKeywords={}, excludeKeywords={}", includeKeywords, excludeKeywords);
    }

    private Optional<String> opt(Map<String,String> map, String key) { return Optional.ofNullable(map.get(key)); }

    private Set<String> parseCsv(String s) {
        if (s == null || s.trim().isEmpty()) return Collections.emptySet();
        String[] arr = s.split(",");
        Set<String> set = new HashSet<>();
        for (String a : arr) {
            String t = a.trim();
            if (!t.isEmpty()) set.add(t);
        }
        return set;
    }

    public boolean allowPayload(String text) {
        if (!includeKeywords.isEmpty()) {
            boolean any = includeKeywords.stream().anyMatch(text::contains);
            if (!any) return false;
        }
        if (!excludeKeywords.isEmpty()) {
            boolean any = excludeKeywords.stream().anyMatch(text::contains);
            if (any) return false;
        }
        return true;
    }
}



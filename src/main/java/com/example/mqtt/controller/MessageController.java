package com.example.mqtt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mqtt.entity.MqttMessage;
import com.example.mqtt.entity.MqttMessageDetail;
import com.example.mqtt.entity.MqttTopic;
import com.example.mqtt.mapper.MqttMessageDetailMapper;
import com.example.mqtt.mapper.MqttMessageMapper;
import com.example.mqtt.mapper.MessageQueryMapper;
import com.example.mqtt.mapper.MqttTopicMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {
    private final MqttMessageMapper messageMapper;
    private final MqttMessageDetailMapper detailMapper;
    private final MessageQueryMapper messageQueryMapper;
    private final MqttTopicMapper topicMapper;

    @GetMapping("/page")
    public ApiResponse<PageResult<MqttMessage>> page(
            @RequestParam(value = "channelName", required = false) String channelName,
            @RequestParam(value = "identifiers", required = false) String identifiers,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(value = "page", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "50") long size,
            @RequestParam(value = "latestOnly", defaultValue = "false") boolean latestOnly
    ) {
        if (size <= 0) size = 50;
        if (size > 500) size = 500;
        if (current <= 0) current = 1;

        List<String> identifierList = parseIdentifiers(identifiers);

        if (latestOnly) {
            long total = messageQueryMapper.countDistinctIdentifiers(channelName, startTime, endTime, identifierList);
            long pages = (total + size - 1) / size;
            long offset = (current - 1) * size;
            List<MqttMessage> records = messageQueryMapper.selectLatestPerIdentifier(
                    channelName, startTime, endTime, identifierList, offset, size
            );
            PageResult<MqttMessage> resp = new PageResult<>();
            resp.setCurrent(current);
            resp.setSize(size);
            resp.setTotal(total);
            resp.setPages(pages);
            resp.setRecords(records);
            return ApiResponse.ok(resp);
        }

        LambdaQueryWrapper<MqttMessage> wrapper = new LambdaQueryWrapper<>();
        // 只查询有效数据
        wrapper.eq(MqttMessage::getStatus, 1);

        if (StringUtils.hasText(channelName)) {
            // 使用前缀/模糊匹配时，索引仍能部分利用，如有需要可按需改为eq
            wrapper.like(MqttMessage::getChannelName, channelName.trim());
        }

        if (!identifierList.isEmpty()) {
            wrapper.in(MqttMessage::getIdentifierValue, identifierList);
        }

        if (startTime != null && endTime != null) {
            wrapper.between(MqttMessage::getReceiveTime, startTime, endTime);
        } else if (startTime != null) {
            wrapper.ge(MqttMessage::getReceiveTime, startTime);
        } else if (endTime != null) {
            wrapper.le(MqttMessage::getReceiveTime, endTime);
        }

        // 利用复合索引的排序顺序
        wrapper.orderByDesc(MqttMessage::getReceiveTime).orderByDesc(MqttMessage::getId);

        Page<MqttMessage> page = new Page<>(current, size);
        IPage<MqttMessage> result = messageMapper.selectPage(page, wrapper);

        PageResult<MqttMessage> resp = new PageResult<>();
        resp.setCurrent(result.getCurrent());
        resp.setSize(result.getSize());
        resp.setTotal(result.getTotal());
        resp.setPages(result.getPages());
        resp.setRecords(result.getRecords());
        return ApiResponse.ok(resp);
    }

    @GetMapping("/detail")
    public ApiResponse<MessageDetailDto> detail(@RequestParam("id") Long id) {
        MqttMessageDetail detail = detailMapper.selectById(id);
        if (detail == null) {
            return ApiResponse.error("未找到对应的消息明细");
        }
        MessageDetailDto dto = new MessageDetailDto();
        dto.setId(String.valueOf(detail.getId()));
        dto.setJsonContent(detail.getJsonContent());
        return ApiResponse.ok(dto);
    }

    @GetMapping("/channels")
    public ApiResponse<List<String>> channels(@RequestParam(value = "keyword", required = false) String keyword,
                                              @RequestParam(value = "limit", defaultValue = "20") int limit) {
        if (limit <= 0) limit = 20;
        if (limit > 100) limit = 100;

        LambdaQueryWrapper<MqttTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(MqttTopic::getTopicName)
                .eq(MqttTopic::getIsEnabled, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(MqttTopic::getTopicName, keyword.trim());
        }
        wrapper.orderByDesc(MqttTopic::getUpdateTime).last("limit " + limit);

        List<MqttTopic> list = topicMapper.selectList(wrapper);
        List<String> names = list.stream()
                .map(MqttTopic::getTopicName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return ApiResponse.ok(names);
    }

    private static List<String> parseIdentifiers(String identifiers) {
        if (!StringUtils.hasText(identifiers)) return Collections.emptyList();
        String norm = identifiers.replace('\n', ',').replace('\r', ',').replace('；', ',').replace('、', ',');
        String[] parts = norm.split(",");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            if (StringUtils.hasText(p)) {
                list.add(p.trim());
                if (list.size() >= 500) break; // 防止过多in列表
            }
        }
        return list;
    }

    @Data
    public static class PageResult<T> {
        private long total;
        private long pages;
        private long current;
        private long size;
        private List<T> records;
    }

    @Data
    public static class MessageDetailDto {
        private String id;
        private String jsonContent;
    }
}



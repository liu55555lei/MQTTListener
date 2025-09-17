package com.example.mqtt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static boolean isValidJson(String content) {
        try {
            OBJECT_MAPPER.readTree(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String pretty(String content) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(content);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            return content;
        }
    }

    public static String extractJsonPath(String json, String dotPath) throws JsonProcessingException {
        JsonNode node = OBJECT_MAPPER.readTree(json);
        String[] parts = dotPath.split("\\.");
        for (String p : parts) {
            if (node == null) return null;
            node = node.get(p);
        }
        return node == null ? null : node.isValueNode() ? node.asText() : node.toString();
    }

    public static String ensureObject(String json) throws JsonProcessingException {
        JsonNode node = OBJECT_MAPPER.readTree(json);
        if (node.isObject()) return json;
        ObjectNode wrapper = OBJECT_MAPPER.createObjectNode();
        wrapper.set("value", node);
        return OBJECT_MAPPER.writeValueAsString(wrapper);
    }
}



package com.example.mqtt;

import com.example.mqtt.service.JsonUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ApiIntegrationTests {

    @Test
    void testJsonUtilsExtractJsonPath() {
        String json = "{\"id\":\"12345\",\"data\":{\"name\":\"test\",\"value\":100}}";
        
        // 测试简单路径
        try {
            String result = JsonUtils.extractJsonPath(json, "id");
            assertEquals("12345", result);
        } catch (Exception e) {
            fail("Failed to extract simple path: " + e.getMessage());
        }
        
        // 测试嵌套路径
        try {
            String result = JsonUtils.extractJsonPath(json, "data.name");
            assertEquals("test", result);
        } catch (Exception e) {
            fail("Failed to extract nested path: " + e.getMessage());
        }
        
        // 测试数字值
        try {
            String result = JsonUtils.extractJsonPath(json, "data.value");
            assertEquals("100", result);
        } catch (Exception e) {
            fail("Failed to extract numeric value: " + e.getMessage());
        }
    }
    
    @Test
    void testJsonUtilsIsValidJson() {
        assertTrue(JsonUtils.isValidJson("{\"id\":\"123\"}"));
        assertTrue(JsonUtils.isValidJson("{\"data\":{\"name\":\"test\"}}"));
        assertFalse(JsonUtils.isValidJson("invalid json"));
        assertFalse(JsonUtils.isValidJson(""));
    }
}

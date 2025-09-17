# MQTT标识位路径功能使用说明

## 功能概述

新增的标识位路径功能允许您从MQTT消息的JSON内容中提取特定字段作为标识，方便快速查询和检索消息。

## 主要特性

1. **标识位路径配置**：为每个监听通道配置标识位路径（如 `id`、`data.id`、`user.name` 等）
2. **自动提取**：系统自动从接收到的JSON消息中提取标识字段值
3. **快速查询**：支持按标识字段值进行模糊查询
4. **优化显示**：查询页面不再直接显示JSON内容，而是显示提取的标识字段

## 使用方法

### 1. 配置标识位路径

1. 访问配置管理页面 (`/config.html`)
2. 在"监听通道管理"部分，添加新的Topic时填写"标识位路径"
3. 标识位路径支持点分隔的嵌套路径，例如：
   - `id` - 提取根级别的id字段
   - `data.id` - 提取data对象中的id字段
   - `user.profile.name` - 提取嵌套对象中的name字段

### 2. 查询消息

1. 访问消息查询页面 (`/query.html`)
2. 使用"标识字段值"输入框进行模糊查询
3. 点击"查看JSON"按钮查看完整的消息内容

### 3. 数据库结构

#### mqtt_topic 表新增字段
- `identifier_path`: 标识位路径配置

#### mqtt_message 表新增字段
- `identifier`: 从JSON中提取的标识字段值
- 新增索引：`idx_identifier` 用于快速查询

## 示例

### JSON消息示例
```json
{
  "id": "MSG001",
  "data": {
    "deviceId": "DEV123",
    "timestamp": "2024-01-01T10:00:00Z"
  },
  "user": {
    "profile": {
      "name": "张三"
    }
  }
}
```

### 标识位路径配置示例
- `id` → 提取值：`MSG001`
- `data.deviceId` → 提取值：`DEV123`
- `user.profile.name` → 提取值：`张三`

## 注意事项

1. 标识位路径区分大小写
2. 如果JSON中不存在指定路径，标识字段将为空
3. 提取的标识值会进行字符串转换
4. 建议为常用的查询字段配置标识位路径以提高查询效率

## 数据库迁移

如果您的系统已有数据，请执行 `migration.sql` 脚本来更新数据库结构：

```sql
-- 为 mqtt_message 表添加 identifier 字段
ALTER TABLE mqtt_message ADD COLUMN identifier VARCHAR(255) DEFAULT NULL COMMENT '从JSON中提取的标识字段值';
CREATE INDEX idx_identifier ON mqtt_message(identifier);

-- 为 mqtt_topic 表添加 identifier_path 字段
ALTER TABLE mqtt_topic ADD COLUMN identifier_path VARCHAR(255) DEFAULT NULL COMMENT '标识位路径（如id、data.id等）';
```

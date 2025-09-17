## 数据库设计

### 表结构
- `mqtt_message`
  - id BIGINT PK AI
  - channel_name VARCHAR(255)
  - data_content JSON
  - receive_time DATETIME DEFAULT CURRENT_TIMESTAMP
  - message_id VARCHAR(64) UNIQUE（可空）
  - status TINYINT (1=正常,0=无效)
  - 索引：idx_channel_time(channel_name, receive_time)

- `system_config`
  - id INT PK AI
  - config_key VARCHAR(64) UNIQUE
  - config_value TEXT
  - config_desc VARCHAR(255)
  - create_time / update_time

- `mqtt_topic`
  - id INT PK AI
  - topic_name VARCHAR(255) UNIQUE
  - is_enabled TINYINT
  - create_time / update_time

### JSON 索引建议
- 针对高频查询键增加函数索引：
```
CREATE INDEX idx_msg_user_age ON mqtt_message ((JSON_UNQUOTE(JSON_EXTRACT(data_content, '$.user.info.age'))));
```

### 分区/分表建议
- 大表按时间范围分区或按月分表（分区键：receive_time），查询范围落单分区。

### 初始化脚本
- 见 `src/main/resources/schema.sql`

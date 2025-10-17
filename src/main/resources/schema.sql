CREATE TABLE IF NOT EXISTS mqtt_message (
  id BIGINT PRIMARY KEY COMMENT '消息ID（雪花算法生成）',
  channel_name VARCHAR(255) NOT NULL COMMENT 'MQTT通道名称（Topic）',
  identifier_value VARCHAR(255) DEFAULT NULL COMMENT '标识位值（从JSON中提取的标识信息）',
  receive_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息接收时间（服务器时间）',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '数据状态：1-正常，0-无效（如格式错误）',
  INDEX idx_receive_time_desc (`receive_time` DESC, `id` DESC),
  INDEX idx_channel_time (`channel_name`, `receive_time` DESC, `id` DESC),
  INDEX idx_identifier_time_desc (`identifier_value`, `receive_time` DESC, `id` DESC),
  INDEX idx_cover_stats (`channel_name`, `identifier_value`, `receive_time` DESC, `id` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQTT消息主表（不包含JSON内容）';

CREATE TABLE IF NOT EXISTS mqtt_message_detail (
  id BIGINT PRIMARY KEY COMMENT '消息ID（与mqtt_message表id保持一致）',
  json_content JSON NOT NULL COMMENT '消息的JSON内容'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQTT消息明细信息表（存储JSON内容）';

CREATE TABLE IF NOT EXISTS system_config (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
  config_key VARCHAR(64) NOT NULL COMMENT '配置键（如mqtt.server_url、mqtt.username）',
  config_value TEXT COMMENT '配置值',
  config_desc VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

CREATE TABLE IF NOT EXISTS mqtt_topic (
  id INT PRIMARY KEY AUTO_INCREMENT COMMENT '通道ID',
  topic_name VARCHAR(255) NOT NULL COMMENT 'MQTT通道名称（Topic）',
  identifier_path VARCHAR(255) DEFAULT NULL COMMENT '标识位路径（如id、data.id等）',
  is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_topic_name (topic_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQTT监听通道配置表';



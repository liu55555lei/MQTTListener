CREATE TABLE IF NOT EXISTS mqtt_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
  channel_name VARCHAR(255) NOT NULL COMMENT 'MQTT通道名称（Topic）',
  data_content JSON NOT NULL COMMENT '消息内容（标准JSON格式，支持嵌套结构）',
  identifier_value VARCHAR(255) DEFAULT NULL COMMENT '标识位值（从JSON中提取的标识信息）',
  receive_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息接收时间（服务器时间）',
  message_id VARCHAR(64) DEFAULT NULL COMMENT 'MQTT消息ID（可选，用于去重）',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '数据状态：1-正常，0-无效（如格式错误）',
  INDEX idx_channel_time (channel_name, receive_time),
  INDEX idx_channel (channel_name),
  INDEX idx_identifier (identifier_value),
  -- MySQL 8 JSON index: create functional indexes as needed in optimization scripts
  UNIQUE KEY uk_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQTT消息持久化表';

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



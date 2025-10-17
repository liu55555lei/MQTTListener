## 设计文档

### 架构与技术栈
- 后端：Java 21、Spring Boot 3.3、MyBatis-Plus、Paho MQTT、Actuator、Retry、Scheduling
- 数据库：MySQL 8.0（JSON 字段）
- 前端：HTML + Bootstrap 5 + Axios（无框架）

### 分层结构
```
web(Controller + static)
service(MQTT、配置、入库、查询)
dao(Mapper + Entity)
common(配置、线程池、拦截器)
```

### 核心流程（时序）
1) MQTT → MqttClientService → 异步线程池 → MessageBatchWriter（批量/去重）→ MySQL
2) 配置保存 → DB(system_config) → ConfigService.reloadAndApply → MQTT 订阅刷新

### 高并发保证
- Paho AsyncClient + 自动重连
- 线程池 `mqttExecutor` 消费消息
- 队列汇聚 + 定时批量入库（可配置批量大小/间隔）

### 可扩展性
- ConfigService 提供 include/exclude 关键字过滤
- JSON 查询通过 JSON_EXTRACT，可扩展为更多路径语法
- DAO 层 MyBatis-Plus，易于替换数据源

### 可用性/监控
- Actuator /health 增加 MQTT 指标
- 定时任务：按天清理历史数据（可配置保留天数）

### 关键类
- `MqttClientService`：连接、订阅、消息分发
- `MessageBatchWriter`：汇聚与批量入库
- `ConfigService`：从 DB 读取并应用配置、关键字过滤
- `DataRetentionJob`：数据保留清理


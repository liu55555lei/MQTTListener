## 接口说明

统一返回：
```
{
  "code": 200,
  "msg": "成功",
  "data": {}
}
```

### 配置管理
- POST /api/mqtt/config/save
  - 入参：JSON（任意键值），常用：
    - app.mqtt.server-uri、app.mqtt.client-id、app.mqtt.username、app.mqtt.password
    - app.mqtt.enabled、app.insert.batch-size、app.insert.batch-interval-ms、app.retention.days
    - app.filter.include、app.filter.exclude（逗号分隔关键字）
  - 出参：{code,msg}

- GET /api/mqtt/config/list
  - 出参：系统配置列表

### Topic 管理
- POST /api/mqtt/topic/add?topicName=xxx
- POST /api/mqtt/topic/toggle?id=1&enabled=0|1
- POST /api/mqtt/topic/delete?id=1
- GET  /api/mqtt/topic/list

### 消息查询与导出
- GET /api/message/query
  - 参数：
    - channelName（可模糊）
    - startTime、endTime（yyyy-MM-dd HH:mm:ss）
    - jsonKey、jsonValue（按 JSON 字段匹配）
    - pageNum、pageSize
  - 返回：分页对象（records/total/pages/current）

- GET /api/message/export
  - 参数：channelName、startTime、endTime
  - 返回：CSV 文件下载

### 健康检查
- GET /actuator/health
  - 包含 MQTT 连接状态与 serverUri


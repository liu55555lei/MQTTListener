## 使用与部署

### 环境
- JDK 21（示例：`C:\home\zulu21.44.17-ca-jdk21.0.8-win_aarch64`）
- Maven 3.9（示例：`C:\home\apache-maven-3.9.11`）
- MySQL 8.0

### 构建
```
mvn clean package -DskipTests=true
```

### 运行
```
java -jar target/mqtt-mysql-service-1.0.0.jar --spring.profiles.active=dev
```
测试环境（按提供的地址）：
```
java -jar target/mqtt-mysql-service-1.0.0.jar --spring.profiles.active=testenv
```

### 配置项（application.yml）
- app.mqtt.enabled：是否启用 MQTT（测试可关闭）
- app.mqtt.server-uri/client-id/username/password
- app.insert.batch-size / app.insert.batch-interval-ms
- app.retention.days：数据保留天数
- app.filter.include / app.filter.exclude：消息包含/排除关键词（逗号分隔）

### 常见问题
- MQTT 连接失败：检查服务器地址、端口、用户名密码；防火墙/网络连通
- 数据写入慢：增大线程池、减小 JSON pretty、调整批量阈值
- JSON 查询无结果：确认 jsonKey 路径，以 `a.b.c` 形式，对应 `$.a.b.c`

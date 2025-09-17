# MQTTListener

一个基于 Spring Boot + MyBatis-Plus 的 MQTT 消息持久化与查询服务。该项目提供 MQTT 客户端订阅与重连、消息批量入库、主题与系统配置管理、简单的 Web 查询界面，以及健康检查与数据保留任务，便于在生产环境中快速落地 MQTT 数据采集与查询。

## 特性
- 批量写入：`MessageBatchWriter` 支持按批大小与时间间隔双触发，高吞吐入库。
- 断线重连：`MqttClientService` 自动重连与订阅恢复。
- 可配置化：通过 `/api/config` 管理 MQTT 与系统参数，持久化在数据库。
- 查询界面：内置 `static/index.html`、`static/query.html`、`static/config.html` 静态页，可离线运行（WebJars）。
- 健康检查：`/actuator/health` 集成 `MqttHealthIndicator`。
- 数据保留：`DataRetentionJob` 定期清理过期消息。

## 目录结构
请见仓库根目录下的 `docs/` 与 `src/`，以及 `pom.xml`、`application-*.yml` 等文件。

## 快速开始
### 环境要求
- JDK 21+
- Maven 3.9+
- MySQL 8+

### 数据库初始化
可使用根目录的 `schema.sql` 初始化表结构。

### 配置
编辑 `src/main/resources/application-dev.yml` 或 `application-prod.yml`：
- `spring.datasource.url` 指向你的数据库，库名建议与 `schema.sql` 对齐。
- `username`、`password` 根据你的数据库修改。
- 其他应用参数见 `application.yml` 的 `app.*`。

### 构建与运行
```bash
mvn clean package -DskipTests
java -jar target/MQTTListener-1.0.0.jar
```

应用默认端口为 `8080`，可通过 `server.port` 修改。

## 使用说明
### 页面
- 配置页：`/config.html`
- 查询页：`/query.html`
- 首页：`/index.html`

### 主要 REST API（部分）
- 主题管理：`/api/topics` GET/POST/DELETE
- 消息查询：`/api/messages` GET（支持条件与分页）
- 系统配置：`/api/config` GET/POST

详情请参考 `docs/api.md`、`docs/usage.md`、`docs/identifier-feature*.md`。

## 部署建议
- 将运行账号的数据库权限最小化，只授予必须的 DML/DDL。
- 配置 `app.retention.days` 合理的保留周期，避免库表无限增长。
- 使用反向代理（如 Nginx）与 TLS 保护管理接口。

## 贡献指南
欢迎 issue 与 PR：
1. 先查重：确认没有重复的 issue/PR。
2. Fork & 分支：从 `main` 新建特性分支。
3. 代码风格：遵循现有格式与命名；不引入未使用的依赖。
4. 提交信息：清晰说明动机、改动点与影响面。
5. 测试：至少通过编译与基础集成测试（如 `ApiIntegrationTests`）。

## 许可协议
本项目采用 MIT 许可证，详见 `LICENSE` 文件。



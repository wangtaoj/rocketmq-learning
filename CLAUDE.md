# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

RocketMQ 学习项目，Maven 聚合工程，父 POM `rocketmq-learning` 统一管理版本，包含两个子模块。

## 构建命令

```bash
# 根目录构建所有模块
mvn clean compile

# 构建单个模块
mvn clean compile -pl rocketmq-basic
mvn clean compile -pl rocketmq-spring

# 运行测试
mvn test
```

## 版本管理

父 POM `dependencyManagement` 统一管理以下版本：

- `spring-boot.version` → `2.7.18`（通过 `spring-boot-dependencies` BOM 管理 Spring Boot 全家桶版本，包括 logback、lombok 等）
- `rocketmq.version` → `5.3.2`（高版本客户端可连接低版本服务端）
- `rocketmq-spring-boot-starter.version` → `2.3.6`（同时支持 Spring Boot 2.x 和 3.x）

子模块依赖**不写版本号**，全部由父 POM 或 BOM 管理。rocketmq-spring 的 starter 依赖由父 POM 管理版本，无需额外排除 client/acl。

## 模块架构

### rocketmq-basic — 原生客户端 API 示例

包路径：`com.wangtao.rocketmq.basic`

每个类是独立的 `main()` 方法运行示例，连接 `127.0.0.1:9876`。

| 子包 | 主题 | 说明 |
|------|------|------|
| `commonmsg` | `commonMsg` | 同步发送与消费 |
| `delaymsg` | `delayMsg` | 延时消息（18个延迟级别），含 SCHEDULE_TOPIC 内部机制详细注释 |
| `ordermsg` | `orderMsg` | 顺序消息，使用 `MessageQueueSelector` 和 `MessageListenerOrderly` |
| `retry` | `retryMsg` | 消费重试：并发消费16次递增延迟重试 vs 顺序消费无限重试 |
| `subscribe.inconsistent` | `subscribe_inconsistent` | 同一消费者组订阅关系不一致导致消息丢失的问题演示 |
| `transactionmsg` | `transactionMsg` | 事务消息，二阶段提交 + 回查机制，半消息存储在 `RMQ_SYS_TRANS_HALF_TOPIC` |

常量定义在 `Constant.java`（NAME_SERVER、topic 名、consumer group 名）。

### rocketmq-spring — Spring Boot 集成示例

包路径：`com.wangtao.rocketmq.spring`

基于 `rocketmq-spring-boot-starter` 的 Web 应用（端口 8080）：

- **controller/MsgController** — REST 接口 `/msg/*`，通过 `RocketMQTemplate` 发送消息
- **listener/** — `@RocketMQMessageListener` 消费者，分别处理 String、MessageExt（带 header）、UserVO 对象
- **vo/UserVO** — Lombok `@Data` 消息对象

## 关键约定

- 两个子模块无代码依赖和共享，但部分 topic（如 `commonMsg`）共用，同时运行时会交互
- Java 版本为 1.8
- 注释使用中文，专业名词保留英文
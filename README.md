# Java WebSocket IRC

一个基于 **Spring Boot + WebSocket** 实现的轻量级 IRC / 实时聊天系统。

项目采用分层架构，将 **WebSocket 传输层、消息协议层、业务应用层、领域模型和基础设施层** 解耦，并使用 **RBAC、MariaDB、Redis** 实现权限控制、聊天记录持久化以及实时状态管理。

本项目主要用于实践和展示 Java 后端开发、实时通信、权限系统、缓存、数据库设计以及网络协议设计能力。

## ✨ Features

* 🔌 基于 Spring WebSocket 的实时双向通信
* 💬 公共频道聊天
* 📝 聊天记录持久化
* 👤 用户管理
* 🔐 RBAC 权限控制
* 🚫 用户封禁 / 解封
* 🔇 用户禁言 / 解禁
* ⚡ Redis 缓存与在线状态管理
* 💾 MariaDB 持久化
* 📡 实时消息广播
* 🔄 WebSocket Session 管理
* 📨 独立的客户端 / 服务端消息协议
* 🛡️ 服务端认证与授权
* 🧩 协议层与业务逻辑解耦

## 🛠️ Tech Stack

| 技术               | 用途                    |
| ---------------- | --------------------- |
| Java             | 后端主要开发语言              |
| Spring Boot      | 应用框架                  |
| Spring WebSocket | WebSocket 实时通信        |
| Spring Security  | 身份认证与安全控制             |
| MariaDB          | 业务数据及聊天记录持久化          |
| Redis            | 缓存、在线状态、Session 等实时数据 |
| RBAC             | 用户角色与权限管理             |

## 🏗️ Architecture

项目采用分层架构，将网络通信、协议解析、业务逻辑和数据访问进行解耦。

```text
                         Client
                            │
                       WebSocket
                            │
                            ▼
                  ┌──────────────────┐
                  │ WebSocket Adapter│
                  └────────┬─────────┘
                           │
                           ▼
                  ┌──────────────────┐
                  │ Protocol Layer   │
                  │ Decode / Encode  │
                  └────────┬─────────┘
                           │
                           ▼
                  ┌──────────────────┐
                  │ Command Handler  │
                  └────────┬─────────┘
                           │
                           ▼
                  ┌──────────────────┐
                  │ Application      │
                  │ Services         │
                  └────────┬─────────┘
                           │
            ┌──────────────┼──────────────┐
            ▼              ▼              ▼
        Security         Chat          Channel
            │              │              │
            └──────────────┼──────────────┘
                           │
                  ┌────────┴────────┐
                  ▼                 ▼
               Redis             MariaDB
```

### Layer Responsibilities

#### Transport Layer

负责 WebSocket 连接生命周期以及客户端连接管理。

```text
WebSocket
    │
    ├── Connect
    ├── Authenticate
    ├── Receive
    ├── Send
    └── Disconnect
```

该层不直接处理具体聊天业务。

#### Protocol Layer

负责客户端与服务端之间的消息协议。

```text
Client Message
       │
       ▼
   Decoder
       │
       ▼
   Message
       │
       ▼
   Handler
```

服务端消息同样经过协议编码后发送给客户端。

#### Application Layer

负责具体业务流程，例如：

* 发送消息
* 加入频道
* 离开频道
* 用户管理
* 封禁
* 禁言
* 权限检查

Application Layer 不应该依赖具体的 WebSocket 实现。

#### Domain Layer

描述 IRC 系统中的核心业务对象，例如：

```text
User
Channel
Message
Role
Permission
Ban
Mute
Session
```

#### Infrastructure Layer

负责外部基础设施：

```text
MariaDB
Redis
WebSocket
```

业务逻辑尽量不直接依赖具体基础设施实现。

---

# 📦 Protocol

项目借鉴游戏网络协议的设计思路，将客户端和服务端之间的通信抽象为独立的 Message / Packet。

```text
protocol/
├── client/
│   ├── LoginMessage
│   ├── ChatSendMessage
│   ├── JoinChannelMessage
│   └── LeaveChannelMessage
│
├── server/
│   ├── LoginSuccessMessage
│   ├── LoginFailureMessage
│   ├── ChatMessage
│   ├── SystemMessage
│   └── ErrorMessage
│
├── codec/
└── handler/
```

### Client → Server

例如发送聊天消息：

```json
{
  "type": "CHAT_SEND",
  "requestId": "abc123",
  "channel": "public",
  "content": "Hello!"
}
```

### Server → Client

服务器广播：

```json
{
  "type": "CHAT_MESSAGE",
  "messageId": 12345,
  "channel": "public",
  "sender": "Alice",
  "content": "Hello!",
  "timestamp": 1720000000
}
```

### Error

请求失败时，可以通过 `requestId` 将错误与原始请求关联：

```json
{
  "type": "ERROR",
  "requestId": "abc123",
  "code": "MUTED",
  "message": "You are currently muted."
}
```

这种设计可以使协议层独立于具体业务实现，并方便未来增加新的客户端。

---

# 👤 User & Session

项目将用户和网络连接分离。

```text
User
 │
 ├── Session
 ├── Session
 └── Session
```

同一个用户可以同时拥有多个 WebSocket 连接，例如：

```text
Alice
 ├── Browser
 ├── Desktop Client
 └── Mobile Client
```

`User` 表示业务层用户，而 `Session` 表示一次具体的网络连接。

这也便于使用 Redis 管理在线状态和 Session 映射。

---

# 💬 Channel

Channel 是 IRC 的核心领域对象。

```text
Channel
├── name
├── members
└── messages
```

例如：

```text
#general
#java
#gaming
#offtopic
```

用户通过加入 Channel 接收其中的消息。

```text
User
 │
 └── ChannelMember
          │
          ▼
       Channel
```

未来可以进一步支持：

* Channel Owner
* Channel Moderator
* Channel-specific permissions
* Private Channel
* Channel password
* Channel invite

---

# 🔐 Authentication & RBAC

项目使用认证机制识别用户身份，并使用 RBAC 控制系统级权限。

基本关系：

```text
User
 │
 └── Role
      │
      └── Permission
```

例如：

| Role      | Permission       |
| --------- | ---------------- |
| USER      | 发送消息、加入频道、查看历史消息 |
| MODERATOR | 禁言、解除禁言          |
| ADMIN     | 用户管理、封禁、解封、权限管理  |

权限检查发生在服务端。

```text
Client Request
      │
      ▼
Authentication
      │
      ▼
Authorization / RBAC
      │
      ▼
Business Logic
```

客户端不能通过修改消息内容绕过服务端权限检查。

---

# 🚫 Moderation

## Ban

封禁记录不会简单地作为 `User.banned = true` 存储，而是作为独立的业务记录。

```text
Ban
├── userId
├── operatorId
├── reason
├── createdAt
└── expiresAt
```

这样可以记录：

* 谁执行了封禁
* 封禁原因
* 封禁开始时间
* 封禁结束时间

## Mute

禁言与封禁类似：

```text
Mute
├── userId
├── operatorId
├── reason
├── createdAt
└── expiresAt
```

被禁言用户仍然可以查看聊天内容，但无法发送消息。

---

# 💾 Data Storage

## MariaDB

MariaDB 用于保存核心持久化数据：

* 用户
* 角色
* 权限
* 用户角色关系
* 角色权限关系
* Channel
* Channel Member
* 聊天记录
* Ban
* Mute

建议数据库统一使用：

```sql
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci
```

这样可以正确存储 Unicode 文本以及 Emoji，并使普通文本比较保持大小写不敏感。

例如用户名：

```text
Alice
alice
ALICE
```

可以在数据库唯一约束下视为同一个用户名。

## Redis

Redis 用于保存实时性较高的数据，例如：

* 在线用户
* WebSocket Session
* 用户状态
* Ban / Mute 缓存
* 高频访问数据
* 临时数据

Redis 不作为核心业务数据的唯一持久化来源。

---

# 📨 Message Flow

普通聊天消息的处理流程：

```text
Client
  │
  │ WebSocket
  ▼
WebSocket Adapter
  │
  ▼
Protocol Decoder
  │
  ▼
Chat Message Handler
  │
  ▼
Authentication
  │
  ▼
Authorization / RBAC
  │
  ▼
Mute Check
  │
  ▼
Chat Service
  │
  ├───────────────┐
  ▼               ▼
MariaDB          Channel
  │               │
  │               ▼
  │          Online Sessions
  │               │
  └───────────────┴──> WebSocket
                         │
                         ▼
                       Clients
```

---

# 🗂️ Project Structure

```text
src/
└── main/
    ├── java/
    │   └── com/example/irc/
    │       │
    │       ├── application/
    │       │   ├── chat/
    │       │   ├── channel/
    │       │   ├── user/
    │       │   └── moderation/
    │       │
    │       ├── domain/
    │       │   ├── user/
    │       │   ├── channel/
    │       │   ├── message/
    │       │   └── moderation/
    │       │
    │       ├── protocol/
    │       │   ├── client/
    │       │   ├── server/
    │       │   ├── codec/
    │       │   └── handler/
    │       │
    │       ├── infrastructure/
    │       │   ├── websocket/
    │       │   ├── redis/
    │       │   └── mariadb/
    │       │
    │       ├── security/
    │       │   ├── authentication/
    │       │   └── authorization/
    │       │
    │       └── config/
    │
    └── resources/
        ├── application.yml
        └── ...
```

---

# 🚀 Getting Started

## Requirements

* Java
* Spring Boot
* MariaDB
* Redis
* Git

## Clone

```bash
git clone https://github.com/your-name/your-repository.git
cd your-repository
```

## Database

创建 MariaDB 数据库：

```sql
CREATE DATABASE irc
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

然后配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/irc
    username: your_username
    password: your_password
```

## Redis

启动 Redis：

```bash
redis-server
```

并在 Spring Boot 配置中设置 Redis 连接。

## Run

使用 Maven：

```bash
./mvnw spring-boot:run
```

或者：

```bash
./mvnw test
```

具体启动参数以项目当前版本配置为准。

---

# 🧪 Testing

计划覆盖：

* Authentication
* RBAC
* WebSocket Connection
* Protocol Encoding / Decoding
* Chat Service
* Channel Management
* Ban / Unban
* Mute / Unmute
* Redis
* MariaDB
* WebSocket Integration Test
* Concurrent Connection Test

---

# 🛣️ Roadmap

* [x] Spring Boot 基础框架
* [x] WebSocket 基础通信
* [ ] Protocol / Message 系统
* [ ] 用户认证
* [ ] RBAC
* [ ] 公共 Channel
* [ ] 聊天记录
* [ ] Redis Session
* [ ] Ban / Unban
* [ ] Mute / Unmute
* [ ] 多 Channel
* [ ] 私聊
* [ ] Channel Permission
* [ ] WebSocket 心跳
* [ ] 自动重连
* [ ] 消息分页
* [ ] 消息撤回
* [ ] 管理后台
* [ ] Docker / Docker Compose
* [ ] API Documentation
* [ ] 性能测试
* [ ] 并发测试

---

# 🎯 Project Goals

本项目不仅实现一个聊天服务器，同时用于实践以下后端开发能力：

* Java / Spring Boot
* WebSocket 实时通信
* 网络消息协议设计
* 分层架构
* RBAC 权限模型
* Authentication / Authorization
* Redis 缓存与 Session 管理
* MariaDB 数据建模
* 实时消息广播
* 并发连接管理
* WebSocket 生命周期管理
* 单元测试与集成测试
* Docker 化部署

---

# 🤝 Contributing

欢迎提交 Issue、Pull Request 或改进建议。

提交代码时建议：

* 保持现有项目结构
* 为核心业务逻辑添加测试
* 不提交数据库密码、Redis 密码等敏感信息
* 权限相关修改应添加对应测试
* 协议修改应同步更新文档

---

# 📄 License

本项目采用 **BSD 3-Clause License**。

SPDX Identifier:

```text
BSD-3-Clause
```

完整许可证文本请见 [`LICENSE`](LICENSE)。

---

## ⭐ About

这是一个面向学习、实践以及后端开发能力展示的开源 IRC 项目。

项目重点关注：

```text
Real-time Communication
        +
Protocol Design
        +
Security
        +
Data Persistence
        +
Caching
        +
Scalable Architecture
```

欢迎 Star ⭐

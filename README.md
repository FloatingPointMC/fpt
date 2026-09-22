# Java WebSocket IRC

一个基于 **Java + WebSocket** 实现的轻量级实时 IRC / 聊天系统。

项目以实时通信为核心，使用 WebSocket 实现客户端与服务端之间的双向通信，并结合 **RBAC、MariaDB、Redis** 实现用户权限管理、聊天记录持久化以及在线状态与高频数据缓存。

> 本项目主要用于学习、实践和展示实时通信、权限控制、缓存与数据库设计等后端开发能力。

## ✨ Features

* 🔌 基于 WebSocket 的实时双向通信
* 💬 公共聊天室 / 公共频道
* 📝 聊天记录持久化
* 👤 用户管理
* 🔐 RBAC（Role-Based Access Control）权限控制
* 🚫 用户封禁 / 解封
* 🔇 用户禁言 / 解除禁言
* ⚡ Redis 缓存
* 💾 MariaDB 持久化存储
* 🕐 在线状态管理
* 📡 实时消息广播
* 🛡️ 服务端权限校验

## 🏗️ Tech Stack

| 技术        | 用途               |
| --------- | ---------------- |
| Java      | 后端主要开发语言         |
| WebSocket | 实时双向通信           |
| MariaDB   | 用户、权限、聊天记录等持久化数据 |
| Redis     | 缓存、在线状态及高频访问数据   |
| RBAC      | 用户角色与权限管理        |

### Architecture

```text
                    ┌───────────────┐
                    │    Client     │
                    │ WebSocket/HTTP│
                    └───────┬───────┘
                            │
                            │ WebSocket
                            ▼
                    ┌───────────────┐
                    │  Java Server  │
                    │               │
                    │ Auth / RBAC   │
                    │ Chat Service  │
                    │ Message Bus   │
                    └───────┬───────┘
                            │
                 ┌──────────┴──────────┐
                 │                     │
                 ▼                     ▼
          ┌──────────────┐      ┌──────────────┐
          │    Redis     │      │   MariaDB    │
          │              │      │              │
          │ Cache        │      │ Users        │
          │ Online State │      │ Roles        │
          │ Session Data │      │ Permissions  │
          │              │      │ Messages     │
          └──────────────┘      └──────────────┘
```

## 💬 Core Features

### Public Chat

用户连接 WebSocket 后可以进入公共聊天频道。

消息经过服务端处理后广播给当前频道中的在线用户。

```text
Client A ──┐
           │
Client B ──┼──> WebSocket Server ──> Public Channel
           │
Client C ──┘
```

服务端负责：

1. 验证用户身份
2. 检查用户是否被封禁或禁言
3. 校验发送权限
4. 保存聊天记录
5. 广播消息

### Chat History

聊天消息会持久化到 MariaDB，方便进行历史消息查询。

典型消息数据包括：

* 消息 ID
* 发送者 ID
* 频道 ID
* 消息内容
* 创建时间

示例：

```text
User A
  │
  │ "Hello!"
  ▼
WebSocket Server
  │
  ├──> Redis
  │
  └──> MariaDB
          │
          ▼
      Chat History
```

### RBAC

项目采用 **Role-Based Access Control** 管理用户权限。

基本模型：

```text
User
 │
 └── Role
      │
      ├── Permission
      ├── Permission
      └── Permission
```

例如可以定义：

| Role      | 权限             |
| --------- | -------------- |
| USER      | 发送消息、查看聊天记录    |
| MODERATOR | 禁言用户、解除禁言      |
| ADMIN     | 用户管理、封禁用户、权限管理 |

实际权限可以根据项目需求继续扩展。

### Ban

管理员可以对用户进行封禁。

被封禁用户无法正常使用聊天服务，服务端会在连接和消息处理阶段进行权限检查。

```text
User
 │
 ▼
Authentication
 │
 ▼
Ban Check
 │
 ├── Banned ──> Reject
 │
 └── Normal ──> Continue
```

### Mute

禁言用于限制用户发送消息，但不影响其查看公共聊天内容。

```text
User
 │
 ▼
Send Message
 │
 ▼
Mute Check
 │
 ├── Muted ──> Reject Message
 │
 └── Normal ──> Broadcast
```

## 🗄️ Data Storage

### MariaDB

MariaDB 负责持久化核心业务数据，例如：

* 用户
* 角色
* 权限
* 用户角色关系
* 角色权限关系
* 聊天消息
* 封禁记录
* 禁言记录

示意关系：

```text
users
  │
  └── user_roles
          │
          ▼
        roles
          │
          └── role_permissions
                    │
                    ▼
               permissions
```

聊天数据：

```text
users
  │
  └────────── messages
                  │
                  ├── channel_id
                  ├── content
                  └── created_at
```

### Redis

Redis 用于存储访问频率较高、实时性较强的数据，例如：

* 在线用户状态
* WebSocket Session 映射
* 用户禁言 / 封禁状态缓存
* 高频访问数据
* 临时会话数据

Redis 中的数据并不作为核心业务数据的唯一持久化来源。

## 🔐 Security

服务端不会仅依赖客户端进行权限判断。

所有涉及权限的操作都会在服务端进行校验：

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
User Status Check
      │
      ▼
Business Logic
      │
      ▼
Database / Redis
```

因此客户端即使构造非法请求，也无法绕过服务端的权限控制。

## 📁 Project Structure

项目结构会根据实际实现持续调整，目前推荐采用类似以下结构：

```text
src/
├── main/
│   ├── java/
│   │   └── ...
│   │       ├── controller/
│   │       ├── websocket/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── model/
│   │       ├── security/
│   │       ├── config/
│   │       └── util/
│   │
│   └── resources/
│       ├── application.yml
│       └── ...
│
└── test/
    └── ...
```

### Module Responsibilities

| Module       | Responsibility    |
| ------------ | ----------------- |
| `websocket`  | WebSocket 连接及消息处理 |
| `controller` | HTTP API          |
| `service`    | 核心业务逻辑            |
| `repository` | 数据库访问             |
| `model`      | 数据模型              |
| `security`   | 身份认证与 RBAC        |
| `config`     | 系统配置              |
| `util`       | 通用工具              |

## 🚀 Getting Started

### Requirements

开始运行项目之前，请准备：

* Java
* MariaDB
* Redis
* Git

### Clone

```bash
git clone https://github.com/your-name/your-repository.git
cd your-repository
```

### Configure Database

创建 MariaDB 数据库：

```sql
CREATE DATABASE irc
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

然后根据实际配置修改数据库连接信息。

### Configure Redis

确保 Redis 服务已经启动：

```bash
redis-server
```

然后在项目配置文件中填写 Redis 连接信息。

### Run

根据项目使用的构建工具执行：

```bash
# Maven
./mvnw spring-boot:run
```

或者：

```bash
# Gradle
./gradlew bootRun
```

> 上述命令取决于项目实际使用的构建系统，请以仓库中的配置为准。

## 📡 WebSocket

WebSocket Endpoint 示例：

```text
ws://localhost:8080/ws
```

客户端连接后即可进行实时消息通信。

> Endpoint、消息格式以及认证方式以当前版本代码实现为准。

## 📨 Message Flow

一条普通聊天消息的处理流程：

```text
Client
  │
  │ WebSocket Message
  ▼
WebSocket Handler
  │
  ▼
Authentication
  │
  ▼
RBAC / User Status Check
  │
  ├── Forbidden ──> Error Response
  │
  ▼
Chat Service
  │
  ├──> MariaDB
  │      └── Save Message
  │
  └──> WebSocket Broadcast
             │
             ▼
        Online Clients
```

## 🧪 Testing

项目将逐步补充以下测试：

* 用户认证测试
* RBAC 权限测试
* WebSocket 消息测试
* 聊天记录持久化测试
* 封禁 / 解封测试
* 禁言 / 解禁测试
* Redis 缓存测试
* 异常场景测试

运行测试：

```bash
./mvnw test
```

## 🛣️ Roadmap

目前项目主要关注基础实时聊天功能，后续计划：

* [ ] 私聊
* [ ] 多频道支持
* [ ] 聊天室管理
* [ ] 消息分页
* [ ] 消息撤回
* [ ] 用户在线状态
* [ ] WebSocket 心跳与断线重连
* [ ] 更完善的权限管理
* [ ] 管理后台
* [ ] Docker 部署
* [ ] Docker Compose 一键启动
* [ ] 单元测试与集成测试
* [ ] API 文档
* [ ] 性能测试
* [ ] WebSocket 并发测试

## 🤝 Contributing

欢迎提交 Issue、Pull Request 或提出改进建议。

如果你希望贡献代码：

```bash
git checkout -b feature/your-feature
```

完成修改并通过测试后提交 Pull Request。

建议在提交代码时：

* 保持代码风格一致
* 为重要业务逻辑补充测试
* 不提交数据库密码、Redis 密码等敏感信息
* 对涉及权限的修改补充权限测试

## 📄 License

本项目采用 **BSD 3-Clause License**。

SPDX Identifier：

```text
BSD-3-Clause
```

完整许可证文本请见 [`LICENSE`](LICENSE)。

BSD 3-Clause 是 OSI 批准的开源许可证，并允许在满足许可证条件的情况下对源代码和二进制形式进行再发布和修改。

---

## ⭐ About This Project

这个项目主要用于实践和展示以下后端开发能力：

* Java 后端开发
* WebSocket 实时通信
* RBAC 权限模型
* Redis 缓存设计
* MariaDB 数据持久化
* 实时消息广播
* 用户状态管理
* 服务端权限校验
* WebSocket 异常处理
* 单元测试与集成测试

如果这个项目对你有帮助，欢迎 Star ⭐

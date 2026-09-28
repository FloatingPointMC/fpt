# FPT

FPT (FloatingPointTransport) is an embeddable and extensible Java network communication and protocol framework.

Simple by default, extensible when necessary.

FPT separates **protocol definition** from **transport implementation**. You define messages and their wire format as a `Protocol`, and FPT handles encoding, decoding, handshake, and transport automatically. The default transport is built on Netty, but the `Protocol` layer does not depend on Netty.

## Features

- Client / Server with Factory → Runtime lifecycle
- Separate `ClientHandler` and `ServerHandler` reflecting different responsibilities
- Immutable, composable `Protocol` definition
- Automatic message codec generation via reflection
- Built-in VarInt / VarLong encoding with ZigZag for signed integers
- Protocol fingerprint (SHA-256) for handshake verification
- `Connection` interface for sending messages
- `ClientConnection` (server side) and `ServerConnection` (client side)
- Configurable `EventGroup` (Netty EventLoopGroup wrapper)
- Netty-based transport (NIO)
- Java 8 compatible

## Tech Stack

| Item       | Value              |
|------------|--------------------|
| Target     | Java 8             |
| Transport  | Netty 4.2.18.Final |
| Build      | Gradle (Kotlin DSL)|

## Modules

```text
fpt/
├── fpt-common/            Protocol, Message, Codec, Fingerprint, MessageRegistry
├── fpt-transport-netty/   Netty transport, EventGroup, Connection
├── fpt-client/            FPTClientFactory, FPTClient, ClientHandler, ServerConnection
└── fpt-server/            FPTServerFactory, FPTServer, ServerHandler, ClientConnection
```

| Module                | Depends on                         | Purpose                        |
|-----------------------|------------------------------------|--------------------------------|
| `fpt-common`          | —                                  | Core protocol and codec definitions |
| `fpt-transport-netty` | `fpt-common`                       | Netty-based transport, Connection, EventGroup |
| `fpt-client`          | `fpt-common`, `fpt-transport-netty`| Client API, ClientHandler, ServerConnection |
| `fpt-server`          | `fpt-common`, `fpt-transport-netty`| Server API, ServerHandler, ClientConnection |

Architecture overview:

```text
                Protocol
                   │
        ┌──────────┴──────────┐
        │                     │
     Client                 Server
        │                     │
        └──────────┬──────────┘
                   │
               Transport
                   │
                 Netty
```

Protocol does not depend on Netty.

## Quick Start

### Server

```java
import io.github.floatingpointmc.fpt.server.FPTServerFactory;
import io.github.floatingpointmc.fpt.server.FPTServer;

FPTServer server = FPTServerFactory.create("0.0.0.0", 25565).run();
```

### Client

```java
import io.github.floatingpointmc.fpt.client.FPTClientFactory;
import io.github.floatingpointmc.fpt.client.FPTClient;

FPTClient client = FPTClientFactory.create("localhost", 25565).connect();
```

This starts a server and connects a client using the default protocol. Both sides use `Protocol.create()`, which has the identifier `"fpt"` and version `1`. The handshake succeeds automatically, and the connection is ready.

### With Messages and Handlers

```java
import io.github.floatingpointmc.fpt.protocol.Protocol;
import io.github.floatingpointmc.fpt.protocol.message.impl.C2SMessage;
import io.github.floatingpointmc.fpt.protocol.message.impl.S2CMessage;
import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.transport.Connection;
import io.github.floatingpointmc.fpt.server.FPTServerFactory;
import io.github.floatingpointmc.fpt.server.FPTServer;
import io.github.floatingpointmc.fpt.server.ServerHandler;
import io.github.floatingpointmc.fpt.client.FPTClientFactory;
import io.github.floatingpointmc.fpt.client.FPTClient;

public class ChatMessage implements C2SMessage {
    public String text;
}

public class ChatBroadcast implements S2CMessage {
    public String text;
}

Protocol protocol = Protocol.create()
        .registerC2S(ChatMessage.class)
        .registerS2C(ChatBroadcast.class);

ServerHandler serverHandler = new ServerHandler() {
    @Override
    public void onConnectionActive(FPTServer server, Connection connection) {
    }

    @Override
    public void onConnectionInactive(FPTServer server, Connection connection) {
    }

    @Override
    public void onMessage(FPTServer server, Connection connection, Message message) {
        if (message instanceof ChatMessage) {
            ChatMessage chat = (ChatMessage) message;
            // handle incoming chat from a client
            ChatBroadcast broadcast = new ChatBroadcast();
            broadcast.text = chat.text;
            connection.send(broadcast);
        }
    }
};

FPTServer server = FPTServerFactory.create("0.0.0.0", 25565, protocol)
        .handler(serverHandler)
        .run();

FPTClient client = FPTClientFactory.create("localhost", 25565, protocol).connect();

ChatMessage msg = new ChatMessage();
msg.text = "hello";
client.getConnection().send(msg);
```

## Architecture

### Factory and Runtime

FPT uses a Factory → Runtime lifecycle model. Configuration is done on the Factory; the Runtime represents the running instance.

```text
FPTServerFactory  ──run()──▶  FPTServer
FPTClientFactory  ──connect()──▶  FPTClient
```

**Configuration must be completed before `run()` or `connect()`.** Once a server is running or a client is connected, configuration (including handlers) cannot be changed.

#### Server Lifecycle

```java
FPTServerFactory factory = FPTServerFactory.create("0.0.0.0", 25565);
// configure: factory.handler(...)

FPTServer server = factory.run();  // returns FPTServer runtime

server.isRunning();       // true
server.getPort();         // 25565
server.getConnections();  // Set<Connection>
```

#### Client Lifecycle

```java
FPTClientFactory factory = FPTClientFactory.create("localhost", 25565);
// configure: factory.handler(...)

FPTClient client = factory.connect();  // returns FPTClient runtime

client.isConnected();      // true
client.getConnection();    // Connection (ServerConnection)

client.disconnect();
client.isConnected();      // false
```

### Server

`FPTServerFactory` static methods:

| Method | Description |
|---|---|
| `FPTServerFactory.create(host, port)` | Create a factory with default protocol |
| `FPTServerFactory.create(host, port, protocol)` | Create a factory with custom protocol |
| `FPTServerFactory.create(host, port, eventGroup)` | Create a factory with custom event group |
| `FPTServerFactory.create(host, port, protocol, eventGroup)` | Create a factory with full configuration |

`FPTServerFactory` instance methods:

| Method | Description |
|---|---|
| `factory.handler(handler...)` | Set `ServerHandler`(s) before running; chainable; throws `IllegalStateException` after `run()` |
| `factory.run()` | Start the server; returns `FPTServer`; throws `IllegalStateException` if already running |

`FPTServer` methods:

| Method | Description |
|---|---|
| `server.isRunning()` | Whether the server is running |
| `server.getPort()` | Actual bound port (useful with port 0) |
| `server.getConnections()` | Immutable set of active `Connection`s |

### Client

`FPTClientFactory` static methods:

| Method | Description |
|---|---|
| `FPTClientFactory.create(host, port)` | Create a factory with default protocol |
| `FPTClientFactory.create(host, port, protocol)` | Create a factory with custom protocol |
| `FPTClientFactory.create(host, port, eventGroup)` | Create a factory with custom event group |
| `FPTClientFactory.create(host, port, protocol, eventGroup)` | Create a factory with full configuration |

`FPTClientFactory` instance methods:

| Method | Description |
|---|---|
| `factory.handler(handler...)` | Set `ClientHandler`(s) before connecting; chainable; throws `IllegalStateException` after `connect()` |
| `factory.connect()` | Connect to the server; returns `FPTClient`; throws `IllegalStateException` if already connected |

`FPTClient` methods:

| Method | Description |
|---|---|
| `client.isConnected()` | Whether the client is connected |
| `client.getConnection()` | The `Connection` (a `ServerConnection`) to the server |
| `client.disconnect()` | Disconnect from the server (idempotent) |

The client and server must use the same protocol definition. The handshake verifies this automatically (see [Handshake](#handshake)).

### Handlers

Client and Server have **separate handler types** reflecting their fundamentally different responsibilities.

#### ServerHandler

`ServerHandler` handles events from connected clients on the server side:

```java
public interface ServerHandler {
    void onConnectionActive(FPTServer server, Connection connection);
    void onConnectionInactive(FPTServer server, Connection connection);
    void onMessage(FPTServer server, Connection connection, Message message);
}
```

Each callback receives the `FPTServer` runtime and the `ClientConnection` representing the connected client. The server handler can inspect server state, manage connections, and send response messages.

#### ClientHandler

`ClientHandler` handles events from the server on the client side:

```java
public interface ClientHandler {
    void onConnectionActive(FPTClient client, Connection connection);
    void onConnectionInactive(FPTClient client, Connection connection);
    void onMessage(FPTClient client, Connection connection, Message message);
}
```

Each callback receives the `FPTClient` runtime and the `ServerConnection` to the server. The client handler can inspect client state and react to server-sent messages.

#### Why Separate Handlers?

The server and client have different lifecycle semantics and different connection types:

```text
ServerHandler
    ↔ FPTServer (manages multiple ClientConnections)
    ↔ ClientConnection (represents a connected client)

ClientHandler
    ↔ FPTClient (owns one ServerConnection)
    ↔ ServerConnection (represents the connected server)
```

A server handler receives `FPTServer` and can access all connections. A client handler receives `FPTClient` and works with its single server connection. These are intentionally separate types — there is no common handler interface.

#### Multiple Handlers

Both server and client support multiple handlers:

```java
FPTServer server = FPTServerFactory.create("0.0.0.0", 25565)
        .handler(chatHandler, systemHandler)
        .run();

FPTClient client = FPTClientFactory.create("localhost", 25565)
        .handler(loggingHandler, gameHandler)
        .connect();
```

#### Handler Lifecycle

Handlers must be configured **before** `run()` or `connect()`:

```java
// Correct: configure before run
FPTServer server = FPTServerFactory.create("0.0.0.0", 25565)
        .handler(myServerHandler)
        .run();
```

```java
// Incorrect: configure after run — throws IllegalStateException
FPTServerFactory factory = FPTServerFactory.create("0.0.0.0", 25565);
FPTServer server = factory.run();
factory.handler(myServerHandler); // IllegalStateException!
```

### Connection

`Connection` represents an active connection and provides the ability to send messages:

```java
public interface Connection {
    boolean isActive();
    void send(Message message) throws IllegalStateException;
    void close();
    SocketAddress getRemoteAddress();
    SocketAddress getLocalAddress();
}
```

FPT provides two `Connection` implementations that reflect the direction of the connection:

| Type | Side | Represents |
|---|---|---|
| `ClientConnection` | Server | A connected client (implements `Connection`) |
| `ServerConnection` | Client | The connected server (implements `Connection`) |

To send a message from the client to the server:

```java
client.getConnection().send(message);
```

To send a message from the server to a specific client:

```java
connection.send(message);  // where connection is from onMessage() or getConnections()
```

### Protocol

A `Protocol` describes a complete communication protocol:

```text
Protocol
├── Identifier    (String)
├── Version       (int)
├── C2S Registry  (MessageRegistry)
├── S2C Registry  (MessageRegistry)
├── Codec Map     (CodecMap)
└── Fingerprint   (SHA-256)
```

#### Creating a Protocol

```java
Protocol protocol = Protocol.create();
// identifier = "fpt", version = 1

Protocol protocol = Protocol.create("myapp", 2);
// identifier = "myapp", version = 2
```

#### Registering Messages

C2S and S2C messages are registered independently. Registration returns a new `Protocol` — the original is never modified:

```java
Protocol protocol = Protocol.create()
        .registerC2S(ChatMessage.class)
        .registerS2C(ChatBroadcast.class);
```

You can register multiple messages at once:

```java
Protocol protocol = Protocol.create()
        .registerC2S(LoginMessage.class, ChatMessage.class)
        .registerS2C(LoginResponse.class, ChatBroadcast.class);
```

#### Immutability

Protocol registration creates a new `Protocol` rather than modifying the existing one:

```java
Protocol base = Protocol.create();

Protocol a = base.registerC2S(ChatMessage.class);
Protocol b = base.registerC2S(LoginMessage.class);
```

```text
base  → no ChatMessage, no LoginMessage
a     → ChatMessage
b     → LoginMessage
```

Each registration returns a new independent protocol. `base` is never modified.

### Message and Codec

#### Messages

A message is a plain Java class that implements `C2SMessage` or `S2CMessage`:

```java
import io.github.floatingpointmc.fpt.protocol.message.impl.C2SMessage;
import io.github.floatingpointmc.fpt.protocol.message.impl.S2CMessage;

public class ChatMessage implements C2SMessage {
    public String sender;
    public String text;
}

public class ChatBroadcast implements S2CMessage {
    public String sender;
    public String text;
}
```

Messages contain only data fields. You do not implement `encode()` or `decode()` — FPT generates codecs automatically via `AutoMessageCodec` using the field types and the protocol's codec map.

Fields must have types supported by the protocol's codec map (see [Default Codecs](#default-codecs)). If a field type has no codec, registration throws `IllegalArgumentException`.

#### Codec

A `Codec<T>` encodes and decodes a Java type to/from a binary wire format:

```java
public abstract class Codec<T> {
    public abstract void encode(ByteBuffer buf, T value) throws EncodeException;
    public abstract T decode(ByteBuffer buf) throws DecodeException;
    public final String identity();
}
```

A codec handles one Java type. A protocol's codec map determines which codec is used for each type. Codec is separate from Protocol — the same codec can be used across different protocols, and different protocols can assign different codecs to the same type.

#### Default Codecs

The default codec map provides:

| Java Type | Codec | Wire Format |
|---|---|---|
| `boolean` / `Boolean` | `fpt:boolean` | 1 byte (0 or 1) |
| `byte` / `Byte` | `fpt:byte` | 1 byte fixed |
| `short` / `Short` | `fpt:short` | 2 bytes fixed (big-endian) |
| `int` / `Integer` | `fpt:int:varint32` | VarInt + ZigZag (variable-length) |
| `long` / `Long` | `fpt:long:varlong64` | VarLong + ZigZag (variable-length) |
| `float` / `Float` | `fpt:float` | 4 bytes fixed (IEEE 754) |
| `double` / `Double` | `fpt:double` | 8 bytes fixed (IEEE 754) |
| `char` / `Character` | `fpt:char` | 2 bytes fixed (big-endian) |
| `String` | `fpt:string:utf8` | VarInt length + UTF-8 bytes (max 32768) |
| `UUID` | `fpt:uuid:128` | 128-bit (two 8-byte longs) |
| `byte[]` | `fpt:bytes` | VarInt length + raw bytes (max 32768) |

#### VarInt and VarLong

`VarInt` and `VarLong` use variable-length encoding with ZigZag for signed integers:

| Value | ZigZag | Encoded bytes |
|---|---|---|
| `0` | `0` | `0x00` |
| `-1` | `1` | `0x01` |
| `1` | `2` | `0x02` |
| `-2` | `3` | `0x03` |
| `2` | `4` | `0x04` |

Small absolute values produce fewer bytes. ZigZag maps signed integers to unsigned so that small negative values also encode compactly.

`VarInt` handles 32-bit integers (`int` / `Integer`). `VarLong` handles 64-bit integers (`long` / `Long`).

#### Fixed32Codec

`Fixed32Codec.INSTANCE` provides a fixed 4-byte big-endian encoding for `Integer`, as an alternative to the default VarInt codec. Use it when you prefer predictable size to compactness.

#### Codec Override

To customize the codec for a type, create a new codec map and pass it to the protocol:

```java
CodecMap customCodecs = new CodecMap(Protocol.create().codec());
customCodecs.put(Integer.class, Fixed32Codec.INSTANCE);

Protocol fixed32Protocol = Protocol.create().codec(customCodecs);
```

Since codec configuration belongs to the protocol, different protocols can use different codecs for the same type:

```text
Protocol A  →  Integer uses VarInt
Protocol B  →  Integer uses Fixed32
```

#### CodecMap

`CodecMap` maps `Class<T>` to `Codec<T>`:

```java
CodecMap map = CodecMap.create();
map.put(Integer.class, Codec.integerCodec());

Codec<Integer> codec = map.get(Integer.class);
```

`CodecMap` has a copy constructor that creates an independent copy:

```java
CodecMap original = CodecMap.create();
original.put(Integer.class, Codec.integerCodec());

CodecMap copy = new CodecMap(original);
// copy is independent: modifying copy does not affect original
```

The default codec map (`Codec.defaultCodecMap()`) returns a new independent copy each time.

#### AutoMessageCodec

When you register a message, FPT automatically generates a `MessageCodec` for it using `AutoMessageCodec`:

```text
Message class
      ↓
AutoMessageCodec.create(messageClass, codecMap)
      ↓
ReflectionMessageCodec
      ↓
Each field → Codec from codecMap
```

You define messages as plain data classes. FPT derives the encoding and decoding from the field types and the protocol's codec map. You never write `encode()` or `decode()` for a message.

If you change the protocol's codec map (e.g., override `Integer` with `Fixed32Codec`), all messages registered after that change will use the new codec for `int` fields.

### Fingerprint

Every protocol has a deterministic fingerprint derived from its definition using SHA-256:

```text
Fingerprint = SHA-256(
    identifier,
    version,
    C2S registry (type names + assigned IDs, in registration order),
    S2C registry (type names + assigned IDs, in registration order),
    codec map (type → codec identity, sorted)
)
```

**Message registration order is part of the protocol definition.** Different registration orders produce different fingerprints:

```java
Protocol ab = Protocol.create()
        .registerC2S(A.class)
        .registerC2S(B.class);

Protocol ba = Protocol.create()
        .registerC2S(B.class)
        .registerC2S(A.class);

// ab.getFingerprint() ≠ ba.getFingerprint()
```

Fingerprints are used during handshake to verify that both sides use the same protocol definition.

### Handshake

When a client connects, FPT performs a protocol handshake before any application messages are exchanged:

```text
TCP connection established
        ↓
Client sends HandshakeMessage
  (identifier, version, fingerprint)
        ↓
Server verifies against its own protocol
        ↓
  Match → Server sends HandshakeMessage back
          → Connection enters normal message exchange
        ↓
  Mismatch → Server closes connection
```

If the identifier, version, or fingerprint do not match, the connection is rejected. Normal message exchange starts only after the handshake succeeds.

The handshake timeout on the client side is 10 seconds.

### Transport

```text
FPT API (Protocol, FPTClientFactory, FPTServerFactory)
              ↓
     Transport abstraction
     (EventGroup, Connection)
              ↓
     Netty implementation
     (fpt-transport-netty)
```

The architecture separates factory configuration from runtime state:

```text
FPTServerFactory (configuration)
        │
        │ run()
        ▼
NettyServerTransport
        │
        │ start()
        ▼
FPTServer (runtime)
    serverChannel, port, connections
```

```text
FPTClientFactory (configuration)
        │
        │ connect()
        ▼
NettyClientTransport
        │
        │ connect()
        ▼
FPTClient (runtime)
    clientChannel, connection
```

The default transport uses Netty with NIO. The pipeline per connection is:

```text
FrameDecoder  →  HandshakeHandler  →  MessageDecoder / MessageEncoder  →  ChannelHandler
```

Users normally do not need to interact directly with Netty's `Channel`, `Pipeline`, `ByteBuf`, or `EventLoop`. FPT manages these internally.

#### EventGroup

`EventGroup` controls the Netty `EventLoopGroup` used for I/O:

```java
EventGroup group = EventGroup.nio();
```

To wrap existing Netty event loop groups:

```java
EventGroup group = EventGroup.wrap(bossGroup, workerGroup);
```

When you pass an `EventGroup` to `FPTServerFactory.create()` or `FPTClientFactory.create()`, you are responsible for closing it. When you use the simplified `create(host, port)` or `create(host, port, protocol)` APIs, FPT creates and owns the event group.

EventGroup is runtime configuration, not part of the protocol definition.

## Installation

FPT is configured for publishing to Maven Central.

Gradle:

```groovy
implementation 'io.github.floatingpointmc:fpt-common:0.1.0'
implementation 'io.github.floatingpointmc:fpt-client:0.1.0'
implementation 'io.github.floatingpointmc:fpt-server:0.1.0'
```

Maven:

```xml
<dependency>
    <groupId>io.github.floatingpointmc</groupId>
    <artifactId>fpt-common</artifactId>
    <version>0.1.0</version>
</dependency>
<dependency>
    <groupId>io.github.floatingpointmc</groupId>
    <artifactId>fpt-client</artifactId>
    <version>0.1.0</version>
</dependency>
<dependency>
    <groupId>io.github.floatingpointmc</groupId>
    <artifactId>fpt-server</artifactId>
    <version>0.1.0</version>
</dependency>
```

You typically need `fpt-common` and either `fpt-client` or `fpt-server` (both of which transitively include `fpt-transport-netty`).

## Building

```bash
./gradlew build
```

Windows:

```powershell
gradlew.bat build
```

Run tests:

```bash
./gradlew test
```

Publish to local Maven repository:

```bash
./gradlew publishToMavenLocal
```

## Java Version

FPT targets Java 8. The library compiles and runs on Java 8 and above.

The Gradle build uses a Java 8 toolchain, so the produced artifacts are Java 8 compatible regardless of the JVM used to run the build.

## Project Status

Early development. Version 0.1.0. The API may change.

## License

LGPL-3.0 — see [LICENSE](LICENSE).
# FPT

FPT (FloatingPointTransport) is an embeddable and extensible Java network communication and protocol framework.

Simple by default, extensible when necessary.

FPT separates **protocol definition** from **transport implementation**. You define messages and their wire format as a `Protocol`, and FPT handles encoding, decoding, handshake, and transport automatically. The default transport is built on Netty, but the `Protocol` layer does not depend on Netty.

## Tech Stack

| Item | Value |
|---|---|
| Library target | Java 8 |
| Gradle JVM | JDK 25 |
| Transport | Netty 4.2.18.Final |
| Build | Gradle (Kotlin DSL) |
| Test | JUnit Jupiter 5.10.2 |
| Dev dependencies | Lombok 1.18.36, JetBrains Annotations 26.1.0 |

## Quick Start

Server:

```java
FPTServer.run("0.0.0.0", 25565);
```

Client:

```java
FPTClient.connect("localhost", 25565);
```

This starts a server and connects a client using the default protocol. Both sides use `Protocol.create()`, which has the identifier `"fpt"` and version `1`. The handshake succeeds automatically, and the connection is ready.

To do anything useful, you need to define messages and register them in a protocol.

## Messages

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

## Protocol

A `Protocol` describes a complete communication protocol:

```text
Protocol
├── Identifier    (String)
├── Version       (int)
├── C2S Registry  (MessageRegistry)
├── S2C Registry  (MessageRegistry)
├── Codec Map     (Map<Class<?>, Codec<?>>)
└── Fingerprint   (SHA-256)
```

### Creating a Protocol

```java
Protocol protocol = Protocol.create();
// identifier = "fpt", version = 1

Protocol protocol = Protocol.create("myapp", 2);
// identifier = "myapp", version = 2
```

### Registering Messages

C2S and S2C messages are registered independently:

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

### Immutability

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

## Server and Client

### Server

```java
FPTServer server = FPTServer.run("0.0.0.0", 25565);
```

With a custom protocol:

```java
Protocol protocol = Protocol.create()
        .registerC2S(ChatMessage.class)
        .registerS2C(ChatBroadcast.class);

FPTServer server = FPTServer.run("0.0.0.0", 25565, protocol);
```

With full control over event group and message listener:

```java
EventGroup eventGroup = EventGroup.nio();
MessageListener listener = new MessageListener() {
    @Override
    public void onConnectionActive(Channel channel) { }

    @Override
    public void onConnectionInactive(Channel channel) { }

    @Override
    public void onMessage(Message message, Channel channel) { }
};

FPTServer server = FPTServer.run("0.0.0.0", 25565, protocol, eventGroup, listener);
```

Server API:

| Method | Description |
|---|---|
| `server.isRunning()` | Whether the server is running |
| `server.getPort()` | Actual bound port (useful with port 0) |
| `server.stop()` | Stop the server |

### Client

```java
FPTClient client = FPTClient.connect("localhost", 25565);
```

With a custom protocol:

```java
FPTClient client = FPTClient.connect("localhost", 25565, protocol);
```

With full control:

```java
FPTClient client = FPTClient.connect("localhost", 25565, protocol, eventGroup, listener);
```

Client API:

| Method | Description |
|---|---|
| `client.isConnected()` | Whether the client is connected |
| `client.send(message)` | Send a message to the server |
| `client.disconnect()` | Disconnect from the server |

The client and server must use the same protocol definition. The handshake verifies this automatically (see [Handshake](#handshake)).

## Codec

A `Codec<T>` encodes and decodes a Java type to/from a binary wire format:

```java
public abstract class Codec<T> {
    public abstract void encode(ByteBuffer buf, T value) throws EncodeException;
    public abstract T decode(ByteBuffer buf) throws DecodeException;
}
```

A codec handles one Java type. A protocol's codec map determines which codec is used for each type. Codec is separate from Protocol — the same codec can be used across different protocols, and different protocols can assign different codecs to the same type.

### Default Codecs

The default codec map (`Codec.DEFAULT_CODEC`) provides:

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

### VarInt and VarLong

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

### Fixed32Codec

`Fixed32Codec.INSTANCE` provides a fixed 4-byte big-endian encoding for `Integer`, as an alternative to the default VarInt codec. Use it when you prefer predictable size over compactness.

### Codec Override

To customize the codec for a type, create a new codec map and pass it to the protocol:

```java
import io.github.floatingpointmc.fpt.codec.Fixed32Codec;

Protocol base = Protocol.create();

Map<Class<?>, Codec<?>> customCodecs = new HashMap<>(base.codec());
customCodecs.put(Integer.class, Fixed32Codec.INSTANCE);

Protocol fixed32Protocol = base.codec(customCodecs);
```

Since codec configuration belongs to the protocol, different protocols can use different codecs for the same type:

```text
Protocol A  →  Integer uses VarInt
Protocol B  →  Integer uses Fixed32
```

## AutoMessageCodec

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

## Fingerprint

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

## Handshake

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

## Transport

```text
FPT API (Protocol, FPTClient, FPTServer)
              ↓
     Transport abstraction
     (EventGroup, MessageListener)
              ↓
     Netty implementation
     (fpt-transport-netty)
```

The default transport uses Netty with NIO. The pipeline per connection is:

```text
FrameDecoder  →  HandshakeHandler  →  MessageDecoder / MessageEncoder  →  ChannelHandler
```

Users normally do not need to interact directly with Netty's `Channel`, `Pipeline`, `ByteBuf`, or `EventLoop`. FPT manages these internally.

## EventGroup

`EventGroup` controls the Netty `EventLoopGroup` used for I/O:

```java
EventGroup group = EventGroup.nio();
```

To wrap existing Netty event loop groups:

```java
EventGroup group = EventGroup.wrap(bossGroup, workerGroup);
```

When you use the `EventGroup.nio()` or `EventGroup.wrap()` factory, you are responsible for closing the group. When you use the simplified `FPTServer.run(host, port)` or `FPTClient.connect(host, port)` APIs, FPT creates and owns the event group.

EventGroup is runtime configuration, not part of the protocol definition.

## MessageListener

`Messenger` receives connection lifecycle events and incoming messages:

```java
public interface MessageListener {
    void onConnectionActive(Channel channel);
    void onConnectionInactive(Channel channel);
    void onMessage(Message message, Channel channel);
}
```

Use `MessageListener.empty()` for a no-op listener.

## Module Structure

```text
fpt/
├── fpt-common/           Protocol, Message, Codec, Fingerprint, MessageRegistry
├── fpt-client/            FPTClient
├── fpt-server/            FPTServer
└── fpt-transport-netty/   Netty transport, EventGroup, MessageListener
```

| Module | Depends on | Purpose |
|---|---|---|
| `fpt-common` | — | Core protocol and codec definitions |
| `fpt-transport-netty` | `fpt-common` | Netty-based transport implementation |
| `fpt-client` | `fpt-common`, `fpt-transport-netty` | Client API |
| `fpt-server` | `fpt-common`, `fpt-transport-netty` | Server API |

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

## Installation

FPT is published to Maven Central.

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

## Build from Source

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

FPT targets Java 8 compatibility. The library compiles and runs on Java 8 and above.

The Gradle build uses a JDK 25 JVM with a Java 8 toolchain, so the build environment requires JDK 25 but the produced artifacts are Java 8 compatible.

## Project Status

Status: Early Development

Version 0.1.0. The API may change.

## License

LGPL-3.0 — see [LICENSE](LICENSE).

## Contributing

Contributions are welcome. The project is at <https://github.com/floatingpointmc/fpt>.
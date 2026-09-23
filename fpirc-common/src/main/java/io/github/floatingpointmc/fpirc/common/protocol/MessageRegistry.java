package io.github.floatingpointmc.fpirc.common.protocol;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class MessageRegistry {
    private final Map<Integer, Entry<?>> idToEntry = new HashMap<>();
    private final Map<Class<?>, Entry<?>> typeToEntry = new HashMap<>();

    public MessageRegistry() {
    }

    public <T extends Message> void register(int packetId, Class<T> messageType, MessageCodec<T> codec) {
        if (packetId < 0) {
            throw new IllegalArgumentException("Packet ID must be non-negative: " + packetId);
        }
        if (idToEntry.containsKey(packetId)) {
            throw new IllegalArgumentException("Packet ID already registered: 0x" + Integer.toHexString(packetId));
        }
        if (typeToEntry.containsKey(messageType)) {
            throw new IllegalArgumentException("Message type already registered: " + messageType.getName());
        }
        Entry<T> entry = new Entry<>(packetId, messageType, codec);
        idToEntry.put(packetId, entry);
        typeToEntry.put(messageType, entry);
    }

    public int getPacketId(Class<? extends Message> messageType) {
        Entry<?> entry = typeToEntry.get(messageType);
        if (entry == null) {
            throw new IllegalArgumentException("Unregistered message type: " + messageType.getName());
        }
        return entry.packetId;
    }

    public Class<? extends Message> getMessageType(int packetId) {
        Entry<?> entry = idToEntry.get(packetId);
        if (entry == null) {
            return null;
        }
        return entry.messageType;
    }

    @SuppressWarnings("unchecked")
    public <T extends Message> @NotNull MessageCodec<T> getCodec(int packetId) {
        Entry<?> entry = idToEntry.get(packetId);
        if (entry == null) {
            throw new NullPointerException("Packet ID " + packetId + " is not registered");
        }
        return (MessageCodec<T>) entry.codec;
    }

    @SuppressWarnings("unchecked")
    public <T extends Message> @NotNull MessageCodec<T> getCodec(Class<T> messageType) {
        Entry<?> entry = typeToEntry.get(messageType);
        if (entry == null) {
            throw new NullPointerException("Unregistered message: " + messageType.getName());
        }
        return (MessageCodec<T>) entry.codec;
    }

    public boolean isRegistered(int packetId) {
        return idToEntry.containsKey(packetId);
    }

    public boolean isRegistered(Class<? extends Message> messageType) {
        return typeToEntry.containsKey(messageType);
    }

    private static final class Entry<T extends Message> {
        private final int packetId;
        private final Class<T> messageType;
        private final MessageCodec<T> codec;

        Entry(int packetId, Class<T> messageType, MessageCodec<T> codec) {
            this.packetId = packetId;
            this.messageType = messageType;
            this.codec = codec;
        }
    }
}
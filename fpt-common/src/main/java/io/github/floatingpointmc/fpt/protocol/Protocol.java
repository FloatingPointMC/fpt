package io.github.floatingpointmc.fpt.protocol;

import io.github.floatingpointmc.fpt.codec.AutoMessageCodec;
import io.github.floatingpointmc.fpt.codec.Codec;
import io.github.floatingpointmc.fpt.codec.MessageCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Protocol {

    public static final String DEFAULT_IDENTIFIER = "fpt";
    public static final int DEFAULT_VERSION = 1;

    private final @NotNull String identifier;
    private final int version;
    private final @NotNull MessageRegistry c2sRegistry;
    private final @NotNull MessageRegistry s2cRegistry;
    private final @NotNull Map<Class<?>, Codec<?>> codecMap;
    private final @NotNull Map<Class<?>, MessageCodec<?>> c2sCodecs;
    private final @NotNull Map<Class<?>, MessageCodec<?>> s2cCodecs;
    private final @NotNull Fingerprint fingerprint;

    private Protocol(@NotNull String identifier, int version,
                     @NotNull MessageRegistry c2sRegistry, @NotNull MessageRegistry s2cRegistry,
                     @NotNull Map<Class<?>, Codec<?>> codecMap,
                     @NotNull Map<Class<?>, MessageCodec<?>> c2sCodecs,
                     @NotNull Map<Class<?>, MessageCodec<?>> s2cCodecs) {
        this.identifier = identifier;
        this.version = version;
        this.c2sRegistry = c2sRegistry;
        this.s2cRegistry = s2cRegistry;
        this.codecMap = codecMap;
        this.c2sCodecs = c2sCodecs;
        this.s2cCodecs = s2cCodecs;
        this.fingerprint = Fingerprint.compute(identifier, version, c2sRegistry, s2cRegistry, codecMap);
    }

    public static @NotNull Protocol create() {
        return new Protocol(DEFAULT_IDENTIFIER, DEFAULT_VERSION,
                new MessageRegistry(), new MessageRegistry(),
                Codec.DEFAULT_CODEC,
                Collections.emptyMap(),
                Collections.emptyMap());
    }

    public static @NotNull Protocol create(@NotNull String identifier, int version) {
        return new Protocol(identifier, version,
                new MessageRegistry(), new MessageRegistry(),
                Codec.DEFAULT_CODEC,
                Collections.emptyMap(),
                Collections.emptyMap());
    }

    public @NotNull Protocol register(@NotNull Class<? extends C2SMessage> messageType) {
        MessageCodec<?> msgCodec = AutoMessageCodec.create(messageType, codecMap);
        MessageRegistry newC2s = c2sRegistry.register(messageType);
        Map<Class<?>, MessageCodec<?>> newC2sCodecs = new HashMap<>(c2sCodecs);
        newC2sCodecs.put(messageType, msgCodec);
        return new Protocol(identifier, version, newC2s, s2cRegistry, codecMap,
                Collections.unmodifiableMap(newC2sCodecs), s2cCodecs);
    }

    public @NotNull Protocol registerS2C(@NotNull Class<? extends S2CMessage> messageType) {
        MessageCodec<?> msgCodec = AutoMessageCodec.create(messageType, codecMap);
        MessageRegistry newS2c = s2cRegistry.register(messageType);
        Map<Class<?>, MessageCodec<?>> newS2cCodecs = new HashMap<>(s2cCodecs);
        newS2cCodecs.put(messageType, msgCodec);
        return new Protocol(identifier, version, c2sRegistry, newS2c, codecMap,
                c2sCodecs, Collections.unmodifiableMap(newS2cCodecs));
    }

    public @NotNull Map<Class<?>, Codec<?>> codec() {
        return codecMap;
    }

    public @NotNull Protocol codec(@NotNull Map<Class<?>, Codec<?>> newCodecMap) {
        Map<Class<?>, Codec<?>> snapshot = new LinkedHashMap<>();
        for (Map.Entry<Class<?>, Codec<?>> entry : newCodecMap.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue());
        }
        Map<Class<?>, Codec<?>> immutable = Collections.unmodifiableMap(snapshot);

        Map<Class<?>, MessageCodec<?>> newC2sCodecs = new HashMap<>();
        for (MessageRegistry.Entry entry : c2sRegistry.entries()) {
            MessageCodec<?> msgCodec = AutoMessageCodec.create(entry.messageType(), immutable);
            newC2sCodecs.put(entry.messageType(), msgCodec);
        }

        Map<Class<?>, MessageCodec<?>> newS2cCodecs = new HashMap<>();
        for (MessageRegistry.Entry entry : s2cRegistry.entries()) {
            MessageCodec<?> msgCodec = AutoMessageCodec.create(entry.messageType(), immutable);
            newS2cCodecs.put(entry.messageType(), msgCodec);
        }

        return new Protocol(identifier, version, c2sRegistry, s2cRegistry, immutable,
                Collections.unmodifiableMap(newC2sCodecs),
                Collections.unmodifiableMap(newS2cCodecs));
    }

    public @NotNull String identifier() {
        return identifier;
    }

    public int version() {
        return version;
    }

    public @NotNull MessageRegistry c2sRegistry() {
        return c2sRegistry;
    }

    public @NotNull MessageRegistry s2cRegistry() {
        return s2cRegistry;
    }

    public @NotNull Fingerprint fingerprint() {
        return fingerprint;
    }

    @SuppressWarnings("unchecked")
    public @NotNull <T extends Message> MessageCodec<T> getMessageCodec(@NotNull Class<T> messageType) {
        MessageCodec<?> codec = c2sCodecs.get(messageType);
        if (codec != null) {
            return (MessageCodec<T>) codec;
        }
        codec = s2cCodecs.get(messageType);
        if (codec != null) {
            return (MessageCodec<T>) codec;
        }
        throw new IllegalArgumentException("No codec for message type: " + messageType.getName());
    }

    public @Nullable MessageCodec<?> getMessageCodec(int messageId, @NotNull MessageDirection direction) {
        if (direction == MessageDirection.C2S) {
            Class<? extends Message> type = c2sRegistry.getMessageType(messageId);
            if (type != null) {
                return c2sCodecs.get(type);
            }
        } else {
            Class<? extends Message> type = s2cRegistry.getMessageType(messageId);
            if (type != null) {
                return s2cCodecs.get(type);
            }
        }
        return null;
    }

    public int getMessageId(@NotNull Class<? extends Message> messageType) {
        if (C2SMessage.class.isAssignableFrom(messageType)) {
            return c2sRegistry.getMessageId(messageType);
        }
        if (S2CMessage.class.isAssignableFrom(messageType)) {
            return s2cRegistry.getMessageId(messageType);
        }
        throw new IllegalArgumentException("Message type not C2S or S2C: " + messageType.getName());
    }

    public static final Protocol DEFAULT_PROTOCOL = create();
}
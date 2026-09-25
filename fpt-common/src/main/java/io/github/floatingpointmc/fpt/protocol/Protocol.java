package io.github.floatingpointmc.fpt.protocol;

import io.github.floatingpointmc.fpt.codec.AutoMessageCodec;
import io.github.floatingpointmc.fpt.codec.Codec;
import io.github.floatingpointmc.fpt.codec.MessageCodec;
import io.github.floatingpointmc.fpt.protocol.message.Message;
import io.github.floatingpointmc.fpt.protocol.message.impl.C2SMessage;
import io.github.floatingpointmc.fpt.protocol.message.impl.S2CMessage;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Getter
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

    @SafeVarargs
    public final @NotNull Protocol registerC2S(Class<? extends C2SMessage> @NotNull ... messageType) {
        return register(
                messageType,
                c2sRegistry,
                c2sCodecs,
                (registry, codecs) -> new Protocol(
                        identifier,
                        version,
                        registry,
                        s2cRegistry,
                        codecMap,
                        codecs,
                        s2cCodecs
                )
        );
    }

    @SafeVarargs
    public final @NotNull Protocol registerS2C(Class<? extends S2CMessage> @NotNull ... messageType) {
        return register(
                messageType,
                s2cRegistry,
                s2cCodecs,
                (registry, codecs) -> new Protocol(
                        identifier,
                        version,
                        c2sRegistry,
                        registry,
                        codecMap,
                        c2sCodecs,
                        codecs
                )
        );
    }

    private @NotNull Protocol register(
            Class<? extends Message> @NotNull [] messages,
            @NotNull MessageRegistry registry,
            @NotNull Map<Class<?>, MessageCodec<?>> codecs,
            @NotNull BiFunction<
                    MessageRegistry,
                    Map<Class<?>, MessageCodec<?>>,
                    Protocol
                    > factory
    ) {
        MessageRegistry newRegistry = registry;
        Map<Class<?>, MessageCodec<?>> newCodecs =
                new HashMap<>(codecs);

        for (Class<? extends Message> message : messages) {
            MessageCodec<?> msgCodec =
                    AutoMessageCodec.create(message, codecMap);

            newRegistry = newRegistry.register(message);
            newCodecs.put(message, msgCodec);
        }

        return factory.apply(
                newRegistry,
                Collections.unmodifiableMap(newCodecs)
        );
    }

    public @NotNull Map<Class<?>, Codec<?>> codec() {
        return codecMap;
    }

    public @NotNull Protocol codec(@NotNull Map<Class<?>, Codec<?>> newCodecMap) {
        Map<Class<?>, Codec<?>> immutable = Collections.unmodifiableMap(newCodecMap);

        Map<Class<?>, MessageCodec<?>> newC2sCodecs = applyNewCodec(immutable, c2sRegistry);
        Map<Class<?>, MessageCodec<?>> newS2cCodecs = applyNewCodec(immutable, s2cRegistry);

        return new Protocol(identifier, version, c2sRegistry, s2cRegistry, immutable,
                Collections.unmodifiableMap(newC2sCodecs),
                Collections.unmodifiableMap(newS2cCodecs));
    }

    private static Map<Class<?>, MessageCodec<?>> applyNewCodec(Map<Class<?>, Codec<?>> immutable, MessageRegistry c2sRegistry) {
        Map<Class<?>, MessageCodec<?>> newC2sCodecs = new HashMap<>();
        for (MessageRegistry.Entry entry : c2sRegistry.entries()) {
            MessageCodec<?> msgCodec = AutoMessageCodec.create(entry.messageType(), immutable);
            newC2sCodecs.put(entry.messageType(), msgCodec);
        }
        return newC2sCodecs;
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
        if (MessageDirection.C2S.equals(direction)) {
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
}
package io.github.floatingpointmc.fpt.codec;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CodecMap {
    private final Map<Class<?>, Codec<?>> codecs;

    public CodecMap(@NotNull CodecMap other) {
        this(new HashMap<>(other.codecs));
    }

    public <T> CodecMap put(@NotNull Class<T> type, @NotNull Codec<T> codec) {
        codecs.put(type, codec);
        return this;
    }

    public <T> @Nullable Codec<T> get(@NotNull Class<T> type) {
        return cast(codecs.get(type));
    }

    public boolean containsKey(@NotNull Class<?> type) {
        return codecs.containsKey(type);
    }

    public int size() {
        return codecs.size();
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable Codec<T> cast(@Nullable Codec<?> codec) {
        return (Codec<T>) codec;
    }

    public static CodecMap create() {
        return new CodecMap(new HashMap<>());
    }

    public Set<? extends Map.Entry<Class<?>, Codec<?>>> entrySet() {
        return new HashSet<>(codecs.entrySet());
    }
}
package io.github.floatingpointmc.fpt.codec;

import io.github.floatingpointmc.fpt.protocol.Message;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@UtilityClass
public final class AutoMessageCodec {

    public static <T extends Message> @NotNull MessageCodec<T> create(@NotNull Class<T> messageType, @NotNull Map<Class<?>, Codec<?>> codecMap) {
        List<FieldInfo> fields = collectFields(messageType, codecMap);
        return new ReflectionMessageCodec<>(messageType, fields);
    }

    private static List<FieldInfo> collectFields(@NotNull Class<?> messageType, @NotNull Map<Class<?>, Codec<?>> codecMap) {
        List<FieldInfo> fields = new ArrayList<>();
        Class<?> current = messageType;
        List<Class<?>> hierarchy = new ArrayList<>();
        while (current != null && current != Object.class) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }
        Collections.reverse(hierarchy);

        List<Field> allFields = new ArrayList<>();
        for (Class<?> cls : hierarchy) {
            Field[] declared = cls.getDeclaredFields();
            for (Field f : declared) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    allFields.add(f);
                }
            }
        }

        for (Field f : allFields) {
            f.setAccessible(true);
            Class<?> boxed = Codec.box(f.getType());
            Codec<?> codec = codecMap.get(boxed);
            if (codec == null) {
                throw new IllegalArgumentException(
                    "No codec for field '" + f.getName() + "' of type '" + f.getType().getName() +
                    "' in message '" + messageType.getName() + "'");
            }
            fields.add(new FieldInfo(f, codec));
        }

        return fields;
    }

    static final class FieldInfo {
        final Field field;
        final Codec<?> codec;

        FieldInfo(Field field, Codec<?> codec) {
            this.field = field;
            this.codec = codec;
        }
    }

    static final class ReflectionMessageCodec<T extends Message> implements MessageCodec<T> {
        private final Class<T> messageType;
        private final List<FieldInfo> fields;

        ReflectionMessageCodec(Class<T> messageType, List<FieldInfo> fields) {
            this.messageType = messageType;
            this.fields = fields;
        }

        @Override
        public void encode(ByteBuffer buf, T message) throws EncodeException {
            try {
                for (FieldInfo fi : fields) {
                    Object value = fi.field.get(message);
                    encodeValue(fi.codec, buf, value);
                }
            } catch (IllegalAccessException e) {
                throw new EncodeException("Failed to access field during encode", e);
            }
        }

        @SuppressWarnings("unchecked")
        private <V> void encodeValue(Codec<V> codec, ByteBuffer buf, Object value) throws EncodeException {
            codec.encode(buf, (V) value);
        }

        @Override
        public T decode(ByteBuffer buf) throws DecodeException {
            try {
                T instance = messageType.newInstance();
                for (FieldInfo fi : fields) {
                    Object value = decodeValue(fi.codec, buf);
                    fi.field.set(instance, value);
                }
                return instance;
            } catch (InstantiationException e) {
                throw new DecodeException("Failed to instantiate message: " + messageType.getName(), e);
            } catch (IllegalAccessException e) {
                throw new DecodeException("Failed to access field during decode", e);
            }
        }

        private <V> V decodeValue(Codec<V> codec, ByteBuffer buf) throws DecodeException {
            return codec.decode(buf);
        }
    }
}
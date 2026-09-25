package io.github.floatingpointmc.fpt.protocol;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MessageRegistry {

    private final List<Entry> entries;

    MessageRegistry() {
        this.entries = Collections.emptyList();
    }

    MessageRegistry(@NotNull List<Entry> entries) {
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public @NotNull MessageRegistry register(@NotNull Class<? extends Message> messageType) {
        if (isRegistered(messageType)) {
            throw new IllegalArgumentException("Message type already registered: " + messageType.getName());
        }
        List<Entry> newEntries = new ArrayList<>(entries);
        newEntries.add(new Entry(newEntries.size(), messageType));
        return new MessageRegistry(newEntries);
    }

    public int getMessageId(@NotNull Class<? extends Message> messageType) {
        for (Entry entry : entries) {
            if (entry.messageType == messageType) {
                return entry.messageId;
            }
        }
        throw new IllegalArgumentException("Unregistered message type: " + messageType.getName());
    }

    public @Nullable Class<? extends Message> getMessageType(int messageId) {
        if (messageId < 0 || messageId >= entries.size()) {
            return null;
        }
        return entries.get(messageId).messageType;
    }

    public boolean isRegistered(@NotNull Class<? extends Message> messageType) {
        for (Entry entry : entries) {
            if (entry.messageType == messageType) {
                return true;
            }
        }
        return false;
    }

    public boolean isRegistered(int messageId) {
        return messageId >= 0 && messageId < entries.size();
    }

    public int size() {
        return entries.size();
    }

    public @NotNull List<Entry> entries() {
        return entries;
    }

    public static final class Entry {
        private final int messageId;
        private final @NotNull Class<? extends Message> messageType;

        Entry(int messageId, @NotNull Class<? extends Message> messageType) {
            this.messageId = messageId;
            this.messageType = messageType;
        }

        public int messageId() {
            return messageId;
        }

        public @NotNull Class<? extends Message> messageType() {
            return messageType;
        }
    }
}
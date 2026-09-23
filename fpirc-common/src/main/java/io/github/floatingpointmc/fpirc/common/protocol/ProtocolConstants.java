package io.github.floatingpointmc.fpirc.common.protocol;

public final class ProtocolConstants {

    public static final int MAX_PACKET_SIZE = 1 << 20;

    public static final int MAX_USERNAME_LENGTH = 32;

    public static final int MAX_MESSAGE_LENGTH = 4096;

    public static final int MAX_CHANNEL_NAME_LENGTH = 64;

    public static final int MAX_REASON_LENGTH = 256;

    public static final int MAX_STRING_LENGTH = 32768;

    private ProtocolConstants() {
    }
}
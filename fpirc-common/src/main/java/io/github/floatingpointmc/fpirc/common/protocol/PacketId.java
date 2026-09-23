package io.github.floatingpointmc.fpirc.common.protocol;

public final class PacketId {

    public static final int LOGIN = 0x00;
    public static final int LOGIN_RESPONSE = 0x01;
    public static final int CHAT = 0x02;
    public static final int DISCONNECT = 0x03;

    private PacketId() {
    }
}
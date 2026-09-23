package io.github.floatingpointmc.fpirc.common.protocol;

public class MessageDecodeException extends Exception {

    public MessageDecodeException(String message) {
        super(message);
    }

    public MessageDecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
package io.github.floatingpointmc.fpirc.common.protocol;

public class MessageEncodeException extends Exception {

    public MessageEncodeException(String message) {
        super(message);
    }

    public MessageEncodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
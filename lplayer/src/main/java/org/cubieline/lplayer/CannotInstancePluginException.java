package org.cubieline.lplayer;

/**
 * Created by LeiGuoting on 14/11/11.
 */


public class CannotInstancePluginException extends RuntimeException{
    /**
     * Constructs a new {@code RuntimeException} that includes the current stack
     * trace.
     */
    public CannotInstancePluginException() {
        super();
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public CannotInstancePluginException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace,
     * the specified detail message and the specified cause.
     *
     * @param detailMessage the detail message for this exception.
     * @param throwable
     */
    public CannotInstancePluginException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace
     * and the specified cause.
     *
     * @param throwable the cause of this exception.
     */
    public CannotInstancePluginException(Throwable throwable) {
        super(throwable);
    }
}

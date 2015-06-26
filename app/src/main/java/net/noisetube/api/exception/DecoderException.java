package net.noisetube.api.exception;

/**
 * Created by Humberto on 27/05/15.
 */
public class DecoderException extends Exception {

    public DecoderException() {
    }

    public DecoderException(String detailMessage) {
        super(detailMessage);
    }

    public DecoderException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DecoderException(Throwable throwable) {
        super(throwable);
    }
}

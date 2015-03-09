package net.noisetube.api.exception;

/**
 * @author Humberto
 */
public class AuthenticationException extends Exception {

    public AuthenticationException() {
        super("Not logged in");
    }

    public AuthenticationException(String detailMessage) {
        super(detailMessage);
    }

    public AuthenticationException(Throwable throwable) {
        super(throwable);
    }
}

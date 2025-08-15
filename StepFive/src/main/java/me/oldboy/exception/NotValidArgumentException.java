package me.oldboy.exception;

/**
 * Exception raised when an invalid parameter is entered when creating a reserve.
 *
 * This exception extends {@link RuntimeException} to indicate that the entered
 * parameter is invalid, or a combination of both.
 */
public class NotValidArgumentException extends RuntimeException {
    /**
     * Creates a {@code NotValidArgumentException} with a description of the exception.
     * @param msg — a detailed message.
     */
    public NotValidArgumentException(String msg) {
        super(msg);
    }
}

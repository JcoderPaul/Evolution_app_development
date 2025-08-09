package me.oldboy.exception.user_exception;

/**
 * Exception raised in the service layer if the user entered an invalid password.
 *
 * This exception extends {@link RuntimeException} to indicate that the user entered
 * an invalid password.
 */
public class LoginNotFoundException extends RuntimeException {

    /**
     * Creates a {@code LoginNotFoundException} with a description of the exception.
     * @param msg — detailed message.
     */
    public LoginNotFoundException(String msg) {
        super(msg);
    }
}

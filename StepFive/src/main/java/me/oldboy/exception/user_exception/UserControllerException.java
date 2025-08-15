package me.oldboy.exception.user_exception;

/**
 * Exception thrown from the controller layer, in the thread handling
 * any requests for the user entity: user with the name not found,
 * password not correct, for example.
 *
 * This exception extends {@link RuntimeException}, indicating exceptions
 * in the controller layer, after any requests for the user entity.
 */
public class UserControllerException extends RuntimeException {
    /**
     * Creates a {@code UserControllerException} with a description of the exception.
     * @param msg — detailed message.
     */
    public UserControllerException(String msg){
        super(msg);
    }
}

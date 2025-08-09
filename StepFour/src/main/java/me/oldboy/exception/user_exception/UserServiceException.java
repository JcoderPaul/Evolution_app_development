package me.oldboy.exception.user_exception;

/**
 * Exception thrown from the service layer, in the thread handling
 * any requests for the user entity: user not found, e.g.
 *
 * This exception extends {@link RuntimeException}, indicating
 * exceptions in the service layer, e.g. user with the requested
 * id not found.
 */
public class UserServiceException extends RuntimeException {

    /**
     * Creates a {@code UserServiceException} with a description of the exception.
     * @param msg — a detailed message.
     */
    public UserServiceException(String msg){
        super(msg);
    }
}

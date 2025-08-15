package me.oldboy.exception.place_exception;

/**
 * Exception thrown in the controller layer when exceptional
 * situations occur when accessing the place entity: place not
 * found, place does not exist, duplication create, etc.
 *
 * This exception extends {@link RuntimeException}, indicating
 * that the requested user was not found.
 */
public class PlaceControllerException extends RuntimeException {
    /**
     * Creates a {@code PlaceControllerException} with a message describing the exception.
     * @param msg — a detailed message.
     */
    public PlaceControllerException(String msg) {
        super(msg);
    }
}

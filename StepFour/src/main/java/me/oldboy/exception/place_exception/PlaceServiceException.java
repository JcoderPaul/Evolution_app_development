package me.oldboy.exception.place_exception;

/**
 * An exception is thrown in the service layer if incorrect actions
 * are performed when working with the place entity, for example: place
 * with id not found, place with number not found, incorrect time range, etc.
 *
 * This exception extends {@link RuntimeException}, indicating that an
 * error occurred in the service layer when working with the place entity.
 */
public class PlaceServiceException extends RuntimeException {

    /**
     * Creates a {@code PlaceServiceException} with a message describing the exception.
     * @param msg — a detailed message.
     */
    public PlaceServiceException(String msg) {
        super(msg);
    }
}

package me.oldboy.exception.reservation_exception;

/**
 * Exception thrown by controller layer in various situations, such as:
 * reservation does not exist or was not found, reservation is not possible,
 * duplicate reservation, insufficient rights to make a reservation, etc.
 *
 * This exception extends {@link RuntimeException}, indicating that an error
 * occurred in the reservation thread controller layer.
 */
public class ReservationControllerException extends RuntimeException {
    /**
     * Creates a {@code ReservationServiceException} with a message describing the exception.
     * @param msg — a detailed message.
     */
    public ReservationControllerException(String msg) {
        super(msg);
    }
}

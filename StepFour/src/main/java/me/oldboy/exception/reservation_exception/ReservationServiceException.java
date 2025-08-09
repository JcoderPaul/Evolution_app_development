package me.oldboy.exception.reservation_exception;

/**
 * Exception thrown from the service layer of the reservation
 * sequence in case of, for example: reservation not possible,
 * reservation not found, update not possible, etc.
 *
 * This exception extends {@link RuntimeException}, indicating
 * that an exception occurred in the service layer while working
 * with the reservation entity.
 */
public class ReservationServiceException extends RuntimeException {

    /**
     * Creates a {@code ReservationServiceException} with a message describing the exception.
     * @param msg — a detailed message.
     */
    public ReservationServiceException(String msg) {
        super(msg);
    }
}

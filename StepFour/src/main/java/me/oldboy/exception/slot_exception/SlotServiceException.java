package me.oldboy.exception.slot_exception;

/**
 * An exception is thrown in the service layer if incorrect actions
 * are performed when working with the slot entity, for example: slot
 * with id not found, slot with number not found, incorrect time range, etc.
 *
 * This exception extends {@link RuntimeException}, indicating that an
 * error occurred in the service layer when working with the slot entity.
 */
public class SlotServiceException extends RuntimeException {

    /**
     * Creates a {@code SlotServiceException} with a message describing the exception.
     * @param msg — a detailed message.
     */
    public SlotServiceException(String msg) {
        super(msg);
    }
}

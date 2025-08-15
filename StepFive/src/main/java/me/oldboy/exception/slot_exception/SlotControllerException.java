package me.oldboy.exception.slot_exception;

/**
 * Exception thrown in the controller layer when exceptional
 * situations occur when accessing the slot entity: slot not
 * found, slot does not exist, etc.
 *
 * This exception extends {@link RuntimeException}, indicating
 * that the requested user was not found.
 */
public class SlotControllerException extends RuntimeException {
    /**
     * Creates a {@code SlotControllerException} with a message describing the exception.
     * @param msg — a detailed message.
     */
    public SlotControllerException(String msg) {
        super(msg);
    }
}

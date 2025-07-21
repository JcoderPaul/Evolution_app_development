package me.oldboy.exception;

public class NotValidArgumentException extends RuntimeException {
    public NotValidArgumentException(String msg) {
        super(msg);
    }
}

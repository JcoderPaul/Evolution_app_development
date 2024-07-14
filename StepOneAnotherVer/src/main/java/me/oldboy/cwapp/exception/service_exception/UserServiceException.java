package me.oldboy.cwapp.exception.service_exception;

public class UserServiceException extends IllegalArgumentException {
    public UserServiceException(String msg) {
        super(msg);
    }
}

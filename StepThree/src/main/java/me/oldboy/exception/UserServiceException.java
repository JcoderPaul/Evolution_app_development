package me.oldboy.exception;

public class UserServiceException extends RuntimeException {
    public UserServiceException(String msg){
        super(msg);
    }
}

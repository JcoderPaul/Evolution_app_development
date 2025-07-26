package me.oldboy.exception.user_exception;

public class UserServiceException extends RuntimeException {
    public UserServiceException(String msg){
        super(msg);
    }
}

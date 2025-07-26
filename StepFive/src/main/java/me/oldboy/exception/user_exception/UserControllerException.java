package me.oldboy.exception.user_exception;

public class UserControllerException extends RuntimeException {
    public UserControllerException(String msg){
        super(msg);
    }
}

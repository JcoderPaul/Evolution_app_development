package me.oldboy.exception;

public class UserControllerException extends RuntimeException {
    public UserControllerException(String msg){
        super(msg);
    }
}

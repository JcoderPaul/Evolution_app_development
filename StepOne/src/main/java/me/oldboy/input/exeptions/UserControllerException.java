package me.oldboy.input.exeptions;

public class UserControllerException extends IllegalArgumentException {
    public UserControllerException(String message) {
        super(message);
    }
}

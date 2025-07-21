package me.oldboy.exception.user_exception;

public class LoginNotFoundException extends RuntimeException {
    public LoginNotFoundException(String msg) {
        super(msg);
    }
}

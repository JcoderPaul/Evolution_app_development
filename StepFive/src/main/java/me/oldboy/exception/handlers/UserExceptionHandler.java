package me.oldboy.exception.handlers;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.exception.exception_entity.ExceptionResponse;
import me.oldboy.exception.user_exception.LoginNotFoundException;
import me.oldboy.exception.user_exception.UserControllerException;
import me.oldboy.exception.user_exception.UserServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class UserExceptionHandler {
    @ExceptionHandler({
            UserControllerException.class,
            UserServiceException.class,
            LoginNotFoundException.class
    })
    public ResponseEntity<ExceptionResponse> handleExceptions(RuntimeException exception) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(exception.getMessage());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}

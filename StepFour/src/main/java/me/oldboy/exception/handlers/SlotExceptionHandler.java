package me.oldboy.exception.handlers;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.exception.exception_entity.ExceptionResponse;
import me.oldboy.exception.slot_exception.SlotControllerException;
import me.oldboy.exception.slot_exception.SlotServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class SlotExceptionHandler {
    @ExceptionHandler({
            RuntimeException.class,
            SlotControllerException.class,
            SlotServiceException.class
    })
    public ResponseEntity<ExceptionResponse> handleExceptions(RuntimeException exception) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(exception.getMessage());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}

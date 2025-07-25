package me.oldboy.exception.handlers;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.exception.NotValidArgumentException;
import me.oldboy.exception.exception_entity.ExceptionResponse;
import me.oldboy.exception.reservation_exception.ReservationControllerException;
import me.oldboy.exception.reservation_exception.ReservationServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ReservationExceptionHandler {
    @ExceptionHandler({
            ReservationControllerException.class,
            ReservationServiceException.class,
            NotValidArgumentException.class
    })
    public ResponseEntity<ExceptionResponse> handleExceptions(RuntimeException exception) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(exception.getMessage());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}

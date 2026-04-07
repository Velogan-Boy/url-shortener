package io.velan.urlshortener.advices;

import io.velan.urlshortener.constants.GlobalMessages;
import io.velan.urlshortener.dtos.ErrorResponse;
import io.velan.urlshortener.exceptions.UrlNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUrlNotFoundException(UrlNotFoundException ex) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(), GlobalMessages.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), GlobalMessages.VALIDATION_FAILED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        log.info(ex.toString());

        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                GlobalMessages.INTERNAL_SERVER_ERROR,
                GlobalMessages.ERROR_OCCURED);
    }
}

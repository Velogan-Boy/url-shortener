package io.velan.urlshortener.advices;

import io.velan.urlshortener.constants.GlobalMessages;
import io.velan.urlshortener.dtos.ErrorResponse;
import io.velan.urlshortener.exceptions.ExternalServiceException;
import io.velan.urlshortener.exceptions.UrlNotFoundException;
import io.velan.urlshortener.exceptions.UserNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Environment environment;

    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    private boolean isProd() {
        return List.of(environment.getActiveProfiles()).contains("prod");
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(UserNotFoundException ex) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.value(), GlobalMessages.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(AccessDeniedException ex) {
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Access Denied", ex.getMessage());
    }

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

    @ExceptionHandler(ExternalServiceException.class)
    public ErrorResponse handleExternalServiceException(ExternalServiceException ex) {
        return new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                GlobalMessages.EXTERNAL_SERVER_ERROR,
                ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {

        log.error("Unhandled exception", ex);

        String message = isProd() ? GlobalMessages.ERROR_OCCURED : ex.getMessage();

        String stackTrace = isProd() ? null : getStackTrace(ex);

        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                GlobalMessages.INTERNAL_SERVER_ERROR,
                message,
                stackTrace);
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}

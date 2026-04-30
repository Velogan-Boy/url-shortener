package io.velan.urlshortener.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ExternalServiceException extends RuntimeException {

    @Getter private final Integer status;

    public ExternalServiceException(String message, Integer status) {
        super(message);
        this.status = status;
    }

    public ExternalServiceException(String message) {
        super(message);
        this.status = HttpStatus.SERVICE_UNAVAILABLE.value();
    }
}

package io.velan.urlshortener.exceptions;

import io.velan.urlshortener.constants.ExceptionMessages;
import lombok.Getter;

@Getter
public class UrlNotFoundException extends RuntimeException {

    private final String shortCode;

    public UrlNotFoundException(String shortCode) {
        super(ExceptionMessages.urlNotFoundException(shortCode));
        this.shortCode = shortCode;
    }
}

package io.velan.urlshortener.dtos;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final long timestamp;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

}

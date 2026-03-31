package io.velan.urlshortener.constants;

public class ExceptionMessages {

    public static final String urlNotFoundException(String shortCode) {
        return "Short URL not found for code: " + shortCode;
    }
}

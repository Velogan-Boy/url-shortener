package io.velan.urlshortener.constants;

public class ExceptionMessages {

    public static final String URL_NOT_FOUND_EXCEPTION(String shortCode) {
        return "Short URL not found for code: " + shortCode;
    }

}

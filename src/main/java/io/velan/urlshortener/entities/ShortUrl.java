package io.velan.urlshortener.entities;

import lombok.Getter;

@Getter
public class ShortUrl {

    private final String shortCode;
    private String originalUrl;
    private final long createdAt;
    private long updatedAt;

    public ShortUrl(String shortCode, String originalUrl, long createdAt) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public void updateOriginalUrl(String newUrl) {
        this.originalUrl = newUrl;
        this.updatedAt = System.currentTimeMillis();
    }
}

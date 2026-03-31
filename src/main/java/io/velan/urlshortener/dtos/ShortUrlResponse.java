package io.velan.urlshortener.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class ShortUrlResponse {

    private final String shortCode;
    private final String shortUrl;
    private final String originalUrl;

    // -> no need of setters in this case huh - we build it out and send it out
}

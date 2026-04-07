package io.velan.urlshortener.configs;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import io.velan.urlshortener.exceptions.ExternalServiceException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QrClientConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("Accept", "image/png");
            template.header("User-Agent", "url-shortener-service");
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            switch (response.status()) {
                case 400:
                    return new ExternalServiceException("Invalid QR request", 400);
                case 404:
                    return new ExternalServiceException("QR API not found", 404);
                case 500:
                    return new ExternalServiceException("QR service internal error", 500);
                default:
                    return new ExternalServiceException(
                            "QR service error: " + response.status(), response.status());
            }
        };
    }
}

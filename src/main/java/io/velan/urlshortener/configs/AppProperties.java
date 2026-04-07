package io.velan.urlshortener.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public final class AppProperties {

    @Getter
    @Setter
    public static class Jwt {

        private String secret;
        private long expirationMs;
    }

    private String baseUrl;
    private Jwt jwt = new Jwt();
}

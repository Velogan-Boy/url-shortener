package io.velan.urlshortener.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public final class AppProperties {

      private String baseUrl;

      public String getBaseUrl() {
            return baseUrl;
      }

      public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
      }

}

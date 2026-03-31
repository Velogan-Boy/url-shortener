package io.velan.urlshortener.dtos;

import io.velan.urlshortener.constants.RegEx;
import io.velan.urlshortener.constants.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UpdateShortUrlRequest {

    @NotBlank(message = ValidationMessages.ORIGINAL_URL_REQUIRED)
    @Size(max = 2048, message = ValidationMessages.ORIGINAL_URL_MAX)
    @Pattern(regexp = RegEx.URL_PRFIX, message = ValidationMessages.ORIGINAL_URL_SCHEME)
    private String originalUrl;

}

package io.velan.urlshortener.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.velan.urlshortener.configs.AppProperties;
import io.velan.urlshortener.dtos.CreateShortUrlRequest;
import io.velan.urlshortener.dtos.ShortUrlResponse;
import io.velan.urlshortener.dtos.UpdateShortUrlRequest;
import io.velan.urlshortener.entities.ShortUrl;
import io.velan.urlshortener.exceptions.UrlNotFoundException;
import io.velan.urlshortener.repository.UrlRepository;
import io.velan.urlshortener.utils.ShortCodeGenerator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock private UrlRepository urlRepository;

    @Mock private AppProperties appProperties;

    @Mock private ShortCodeGenerator shortCodeGenerator;

    @InjectMocks private UrlService urlService;

    private static final String BASE_URL = "http://localhost:8080";

    @Test
    void shouldCreateShortUrl() {
        when(shortCodeGenerator.generate()).thenReturn("hello123");
        when(appProperties.getBaseUrl()).thenReturn(BASE_URL);
        CreateShortUrlRequest request = new CreateShortUrlRequest("https://google.com/1");

        ShortUrlResponse response = urlService.createShortUrl(request);

        ArgumentCaptor<ShortUrl> captor = ArgumentCaptor.forClass(ShortUrl.class);
        verify(urlRepository)
                .save(captor.capture()); // argument captor capture what was passed as argument

        ShortUrl saved = captor.getValue();

        assertThat(saved.getShortCode()).isEqualTo("hello123");
        assertThat(saved.getOriginalUrl()).isEqualTo(request.getOriginalUrl());

        assertThat(response.getShortCode()).isEqualTo("hello123");
        assertThat(response.getShortUrl()).isEqualTo(BASE_URL + "/hello123");
    }

    @Test
    void shouldReturnShortUrlWhenExists() {
        ShortUrl entity = new ShortUrl("hello123", "/page", 1000L);

        when(urlRepository.findByShortCode("hello123")).thenReturn(Optional.of(entity));
        when(appProperties.getBaseUrl()).thenReturn(BASE_URL);

        ShortUrlResponse response = urlService.getShortUrl("hello123");

        assertThat(response.getShortCode()).isEqualTo("hello123");
        assertThat(response.getOriginalUrl()).isEqualTo(entity.getOriginalUrl());
    }

    @Test
    void shouldThrowWhenShortUrlNotFound() {
        when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        UrlNotFoundException ex =
                assertThrows(UrlNotFoundException.class, () -> urlService.getShortUrl("missing"));

        assertThat(ex.getShortCode()).isEqualTo("missing");
    }

    @Test
    void shouldReturnAllShortUrls() {
        when(urlRepository.findAll())
                .thenReturn(
                        List.of(
                                new ShortUrl("hello123", "/1", 100L),
                                new ShortUrl("xyz789", "/2", 200L)));
        when(appProperties.getBaseUrl()).thenReturn(BASE_URL);

        List<ShortUrlResponse> result = urlService.getAllShortUrls();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ShortUrlResponse::getShortCode)
                .containsExactly("hello123", "xyz789");
    }

    @Test
    void shouldUpdateShortUrl() {
        ShortUrl entity = new ShortUrl("hello123", "http://old.com", 1000L);

        when(urlRepository.findByShortCode("hello123")).thenReturn(Optional.of(entity));
        when(appProperties.getBaseUrl()).thenReturn(BASE_URL);

        UpdateShortUrlRequest request = new UpdateShortUrlRequest("http://new.com");

        ShortUrlResponse response = urlService.updateShortUrl("hello123", request);

        verify(urlRepository).save(entity);

        assertThat(entity.getOriginalUrl()).isEqualTo("http://new.com");
        assertThat(response.getOriginalUrl()).isEqualTo("http://new.com");
    }

    @Test
    void shouldThrowWhenUpdatingMissingUrl() {
        when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThrows(
                UrlNotFoundException.class,
                () ->
                        urlService.updateShortUrl(
                                "missing", new UpdateShortUrlRequest("http://x.com")));

        verify(urlRepository, never()).save(any());
    }

    @Test
    void shouldDeleteShortUrl() {
        when(urlRepository.existsByShortCode("hello123")).thenReturn(true);

        urlService.deleteShortUrl("hello123");

        verify(urlRepository).deleteByShortCode("hello123");
    }

    @Test
    void shouldThrowWhenDeletingMissingUrl() {
        when(urlRepository.existsByShortCode("missing")).thenReturn(false);

        assertThrows(UrlNotFoundException.class, () -> urlService.deleteShortUrl("missing"));

        verify(urlRepository, never()).deleteByShortCode(any());
    }

    @Test
    void shouldReturnOriginalUrl() {
        when(urlRepository.findByShortCode("hello123"))
                .thenReturn(Optional.of(new ShortUrl("hello123", "", 1000L)));

        String result = urlService.getOriginalUrl("hello123");

        assertThat(result).isEqualTo("");
    }

    @Test
    void shouldThrowWhenOriginalUrlNotFound() {
        when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> urlService.getOriginalUrl("missing"));
    }
}

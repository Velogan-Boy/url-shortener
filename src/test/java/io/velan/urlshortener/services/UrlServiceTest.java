// package io.velan.urlshortener.services;

// import java.util.List;
// import java.util.Optional;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import static org.mockito.ArgumentMatchers.any;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import org.mockito.junit.jupiter.MockitoExtension;

// import io.velan.urlshortener.configs.AppProperties;
// import io.velan.urlshortener.dtos.CreateShortUrlRequest;
// import io.velan.urlshortener.dtos.ShortUrlResponse;
// import io.velan.urlshortener.dtos.UpdateShortUrlRequest;
// import io.velan.urlshortener.entities.ShortUrl;
// import io.velan.urlshortener.exceptions.UrlNotFoundException;
// import io.velan.urlshortener.repository.UrlRepository;
// import io.velan.urlshortener.utils.ShortCodeGenerator;

// @ExtendWith(MockitoExtension.class)
// class UrlServiceTest {

//     @Mock private UrlRepository urlRepository;
//     @Mock private AppProperties appProperties;
//     @Mock private ShortCodeGenerator shortCodeGenerator;

//     @InjectMocks private UrlService urlService;

//     private static final String BASE_URL = "http://localhost:8080";

//     @Test
//     void shouldCreateShortUrl() {
//         when(shortCodeGenerator.generate()).thenReturn("hello123");
//         when(urlRepository.existsByShortCode(any())).thenReturn(false);
//         when(appProperties.getBaseUrl()).thenReturn(BASE_URL);

//         CreateShortUrlRequest request = new CreateShortUrlRequest("https://google.com/1");

//         ShortUrlResponse response = urlService.createShortUrl(request);

//         ArgumentCaptor<ShortUrl> captor = ArgumentCaptor.forClass(ShortUrl.class);
//         verify(urlRepository).save(captor.capture());

//         ShortUrl saved = captor.getValue();

//         assertThat(saved.getShortCode()).isEqualTo("hello123");
//         assertThat(saved.getOriginalUrl()).isEqualTo(request.getOriginalUrl());

//         assertThat(response.getShortCode()).isEqualTo("hello123");
//         assertThat(response.getShortUrl()).isEqualTo(BASE_URL + "/hello123");
//     }

//     @Test
//     void shouldReturnShortUrlWhenExists() {
//         ShortUrl entity = new ShortUrl("hello123", "/page");

//         when(urlRepository.findByShortCode("hello123")).thenReturn(Optional.of(entity));
//         when(appProperties.getBaseUrl()).thenReturn(BASE_URL);

//         ShortUrlResponse response = urlService.getShortUrl("hello123");

//         assertThat(response.getShortCode()).isEqualTo("hello123");
//         assertThat(response.getOriginalUrl()).isEqualTo(entity.getOriginalUrl());
//     }

//     @Test
//     void shouldThrowWhenShortUrlNotFound() {
//         when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

//         UrlNotFoundException ex =
//                 assertThrows(UrlNotFoundException.class, () ->
// urlService.getShortUrl("missing"));

//         assertThat(ex.getShortCode()).isEqualTo("missing");
//     }

//     @Test
//     void shouldReturnAllShortUrls() {
//         when(urlRepository.findAll())
//                 .thenReturn(List.of(new ShortUrl("hello123", "/1"), new ShortUrl("xyz789",
// "/2")));
//         when(appProperties.getBaseUrl()).thenReturn(BASE_URL);

//         List<ShortUrlResponse> result = urlService.getAllShortUrls();

//         assertThat(result).hasSize(2);
//         assertThat(result)
//                 .extracting(ShortUrlResponse::getShortCode)
//                 .containsExactly("hello123", "xyz789");
//     }

//     @Test
//     void shouldUpdateShortUrl() {
//         ShortUrl entity = new ShortUrl("hello123", "http://old.com");

//         when(urlRepository.findByShortCode("hello123")).thenReturn(Optional.of(entity));
//         when(appProperties.getBaseUrl()).thenReturn(BASE_URL);

//         UpdateShortUrlRequest request = new UpdateShortUrlRequest("http://new.com");

//         ShortUrlResponse response = urlService.updateShortUrl("hello123", request);

//         verify(urlRepository, never()).save(any());

//         assertThat(entity.getOriginalUrl()).isEqualTo("http://new.com");
//         assertThat(response.getOriginalUrl()).isEqualTo("http://new.com");
//     }

//     @Test
//     void shouldThrowWhenUpdatingMissingUrl() {
//         when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

//         assertThrows(
//                 UrlNotFoundException.class,
//                 () ->
//                         urlService.updateShortUrl(
//                                 "missing", new UpdateShortUrlRequest("http://x.com")));

//         verify(urlRepository, never()).save(any());
//     }

//     @Test
//     void shouldDeleteShortUrl() {
//         ShortUrl entity = new ShortUrl("hello123", "url");

//         when(urlRepository.findByShortCode("hello123")).thenReturn(Optional.of(entity));

//         urlService.deleteShortUrl("hello123");

//         verify(urlRepository).delete(entity);
//     }

//     @Test
//     void shouldThrowWhenDeletingMissingUrl() {
//         when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

//         assertThrows(UrlNotFoundException.class, () -> urlService.deleteShortUrl("missing"));

//         verify(urlRepository, never()).delete(any());
//     }

//     @Test
//     void shouldReturnOriginalUrl() {
//         when(urlRepository.findByShortCode("hello123"))
//                 .thenReturn(Optional.of(new ShortUrl("hello123", "https://x.com")));

//         String result = urlService.getOriginalUrl("hello123");

//         assertThat(result).isEqualTo("https://x.com");
//     }

//     @Test
//     void shouldThrowWhenOriginalUrlNotFound() {
//         when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

//         assertThrows(UrlNotFoundException.class, () -> urlService.getOriginalUrl("missing"));
//     }
// }

package io.velan.urlshortener.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import io.velan.urlshortener.configs.AppProperties;
import io.velan.urlshortener.dtos.CreateShortUrlRequest;
import io.velan.urlshortener.dtos.ShortUrlResponse;
import io.velan.urlshortener.dtos.UpdateShortUrlRequest;
import io.velan.urlshortener.entities.ShortUrl;
import io.velan.urlshortener.entities.User;
import io.velan.urlshortener.exceptions.UrlNotFoundException;
import io.velan.urlshortener.exceptions.UserNotFoundException;
import io.velan.urlshortener.repository.UrlRepository;
import io.velan.urlshortener.repository.UserRepository;
import io.velan.urlshortener.utils.ShortCodeGenerator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UrlService Unit Tests")
class UrlServiceTest {

    @Mock UrlRepository urlRepository;
    @Mock UserRepository userRepository;
    @Mock AppProperties appProperties;
    @Mock ShortCodeGenerator shortCodeGenerator;

    @InjectMocks UrlService urlService;

    private static final Long USER_ID = 1L;
    private static final String SHORT_CODE = "abc123";
    private static final String BASE_URL = "https://short.ly";
    private static final String ORIGINAL = "https://example.com/very/long/url";
    private static final String SHORT_URL = BASE_URL + "/" + SHORT_CODE;

    private User user;
    private ShortUrl shortUrl;

    @BeforeEach
    void setUp() {
        user = new User();
        ReflectionTestUtils.setField(user, "id", USER_ID);

        shortUrl = new ShortUrl(SHORT_CODE, ORIGINAL, user);

        given(appProperties.getBaseUrl()).willReturn(BASE_URL);
    }

    @Nested
    @DisplayName("createShortUrl")
    class CreateShortUrl {

        @Test
        @DisplayName("creates and returns a ShortUrlResponse for a valid user")
        void happyPath() {
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(shortCodeGenerator.generate()).willReturn(SHORT_CODE);
            given(urlRepository.existsByShortCode(SHORT_CODE)).willReturn(false);
            given(urlRepository.save(any(ShortUrl.class))).willReturn(shortUrl);

            CreateShortUrlRequest req = new CreateShortUrlRequest(ORIGINAL);
            ShortUrlResponse resp = urlService.createShortUrl(req, USER_ID);

            assertThat(resp.getShortCode()).isEqualTo(SHORT_CODE);
            assertThat(resp.getShortUrl()).isEqualTo(SHORT_URL);
            assertThat(resp.getOriginalUrl()).isEqualTo(ORIGINAL);

            then(urlRepository).should().save(any(ShortUrl.class));
        }

        @Test
        @DisplayName("throws UserNotFoundException when user does not exist")
        void userNotFound() {
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            CreateShortUrlRequest req = new CreateShortUrlRequest(ORIGINAL);

            assertThatThrownBy(() -> urlService.createShortUrl(req, USER_ID))
                    .isInstanceOf(UserNotFoundException.class);

            then(urlRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("retries code generation when collision detected")
        void codeCollision() {
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(shortCodeGenerator.generate()).willReturn("collide", SHORT_CODE);
            given(urlRepository.existsByShortCode("collide")).willReturn(true);
            given(urlRepository.existsByShortCode(SHORT_CODE)).willReturn(false);
            given(urlRepository.save(any(ShortUrl.class))).willReturn(shortUrl);

            CreateShortUrlRequest req = new CreateShortUrlRequest(ORIGINAL);
            ShortUrlResponse resp = urlService.createShortUrl(req, USER_ID);

            assertThat(resp.getShortCode()).isEqualTo(SHORT_CODE);
            then(shortCodeGenerator).should(times(2)).generate();
        }
    }

    @Nested
    @DisplayName("getShortUrl")
    class GetShortUrl {

        @Test
        @DisplayName("returns response when caller owns the URL")
        void ownerAccess() {
            given(appProperties.getBaseUrl()).willReturn(BASE_URL);

            given(urlRepository.findByShortCode(SHORT_CODE)).willReturn(Optional.of(shortUrl));

            ShortUrlResponse resp = urlService.getShortUrl(SHORT_CODE, USER_ID);

            assertThat(resp.getShortCode()).isEqualTo(SHORT_CODE);
        }

        @Test
        @DisplayName("throws AccessDeniedException when caller does not own the URL")
        void nonOwnerAccess() {
            given(urlRepository.findByShortCode(SHORT_CODE)).willReturn(Optional.of(shortUrl));

            assertThatThrownBy(() -> urlService.getShortUrl(SHORT_CODE, 99L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("throws UrlNotFoundException for unknown short code")
        void notFound() {
            given(urlRepository.findByShortCode(SHORT_CODE)).willReturn(Optional.empty());

            assertThatThrownBy(() -> urlService.getShortUrl(SHORT_CODE, USER_ID))
                    .isInstanceOf(UrlNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllShortUrls")
    class GetAllShortUrls {

        @Test
        @DisplayName("admin receives all URLs")
        void adminGetsAll() {
            given(urlRepository.findAll()).willReturn(List.of(shortUrl));

            List<ShortUrlResponse> result = urlService.getAllShortUrls(USER_ID, true);

            assertThat(result).hasSize(1);
            then(urlRepository).should(never()).findByUserId(any());
        }

        @Test
        @DisplayName("regular user receives only their own URLs")
        void userGetsOwn() {
            given(urlRepository.findByUserId(USER_ID)).willReturn(List.of(shortUrl));

            List<ShortUrlResponse> result = urlService.getAllShortUrls(USER_ID, false);

            assertThat(result).hasSize(1);
            then(urlRepository).should(never()).findAll();
        }

        @Test
        @DisplayName("returns empty list when user has no URLs")
        void emptyForUser() {
            given(urlRepository.findByUserId(USER_ID)).willReturn(List.of());

            List<ShortUrlResponse> result = urlService.getAllShortUrls(USER_ID, false);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateShortUrl")
    class UpdateShortUrl {

        private static final String NEW_URL = "https://example.com/new-url";

        @Test
        @DisplayName("updates and returns response for owner")
        void ownerUpdate() {
            given(urlRepository.findByShortCode(SHORT_CODE)).willReturn(Optional.of(shortUrl));

            UpdateShortUrlRequest req = new UpdateShortUrlRequest(NEW_URL);
            ShortUrlResponse resp = urlService.updateShortUrl(SHORT_CODE, req, USER_ID);

            then(urlRepository).should().save(shortUrl);
            assertThat(resp).isNotNull();
        }

        @Test
        @DisplayName("throws AccessDeniedException for non-owner")
        void nonOwnerUpdate() {
            given(urlRepository.findByShortCode(SHORT_CODE)).willReturn(Optional.of(shortUrl));

            UpdateShortUrlRequest req = new UpdateShortUrlRequest(NEW_URL);

            assertThatThrownBy(() -> urlService.updateShortUrl(SHORT_CODE, req, 99L))
                    .isInstanceOf(AccessDeniedException.class);

            then(urlRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("throws UrlNotFoundException for missing short code")
        void notFound() {
            given(urlRepository.findByShortCode(SHORT_CODE)).willReturn(Optional.empty());

            UpdateShortUrlRequest req = new UpdateShortUrlRequest(NEW_URL);

            assertThatThrownBy(() -> urlService.updateShortUrl(SHORT_CODE, req, USER_ID))
                    .isInstanceOf(UrlNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteShortUrl")
    class DeleteShortUrl {

        @Test
        @DisplayName("owner can delete their own URL")
        void ownerDelete() {
            given(urlRepository.findByShortCode(SHORT_CODE)).willReturn(Optional.of(shortUrl));

            urlService.deleteShortUrl(SHORT_CODE, USER_ID, false);

            then(urlRepository).should().delete(shortUrl);
        }

        @Test
        @DisplayName("admin can delete any URL")
        void adminDelete() {
            given(urlRepository.findByShortCode(SHORT_CODE)).willReturn(Optional.of(shortUrl));

            urlService.deleteShortUrl(SHORT_CODE, 99L, true);

            then(urlRepository).should().delete(shortUrl);
        }

        @Test
        @DisplayName("throws AccessDeniedException for non-owner non-admin")
        void nonOwnerNonAdminDelete() {
            given(urlRepository.findByShortCode(SHORT_CODE)).willReturn(Optional.of(shortUrl));

            assertThatThrownBy(() -> urlService.deleteShortUrl(SHORT_CODE, 99L, false))
                    .isInstanceOf(AccessDeniedException.class);

            then(urlRepository).should(never()).delete(any());
        }

        @Test
        @DisplayName("throws UrlNotFoundException for missing short code")
        void notFound() {
            given(urlRepository.findByShortCode(SHORT_CODE)).willReturn(Optional.empty());

            assertThatThrownBy(() -> urlService.deleteShortUrl(SHORT_CODE, USER_ID, false))
                    .isInstanceOf(UrlNotFoundException.class);
        }
    }
}

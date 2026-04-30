package io.velan.urlshortener.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.velan.urlshortener.dtos.CreateShortUrlRequest;
import io.velan.urlshortener.dtos.UpdateShortUrlRequest;
import io.velan.urlshortener.entities.ShortUrl;
import io.velan.urlshortener.entities.User;
import io.velan.urlshortener.enums.Role;
import io.velan.urlshortener.factories.ShortUrlFactory;
import io.velan.urlshortener.factories.UserFactory;
import io.velan.urlshortener.repository.UrlRepository;
import io.velan.urlshortener.repository.UserRepository;
import io.velan.urlshortener.security.UserPrincipal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("Url Controller Integration Tests")
class UrlControllerIntegrationTest extends BaseIntegrationTest {

    // Endpoints tested:
    private static final String URLS = "/urls";
    private static final String URL_BY_CODE = "/urls/{code}";
    private static final String URL_QR = "/urls/{code}/qr";

    // common test
    private static final String GOOGLE = "https://google.com";
    private static final String NEW_URL = "https://new.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private UrlRepository urlRepository;
    @Autowired private UserRepository userRepository;

    private UserFactory userFactory;
    private ShortUrlFactory shortUrlFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setup() {
        userFactory = new UserFactory(userRepository);
        shortUrlFactory = new ShortUrlFactory(urlRepository, userFactory);
        testUser = userFactory.create();
        auth = auth(testUser);
    }

    private UsernamePasswordAuthenticationToken auth(User user) {
        return new UsernamePasswordAuthenticationToken(
                new UserPrincipal(user.getId(), Role.USER.name()),
                null,
                List.of(() -> "ROLE_USER"));
    }

    private CreateShortUrlRequest createRequest(String url) {
        return CreateShortUrlRequest.builder().originalUrl(url).build();
    }

    private UpdateShortUrlRequest updateRequest(String url) {
        return UpdateShortUrlRequest.builder().originalUrl(url).build();
    }

    @Nested
    @DisplayName("POST /urls")
    class CreateShortUrl {

        @Test
        @DisplayName("should create short url successfully")
        void shouldCreateShortUrl() throws Exception {

            mockMvc.perform(
                            post(URLS)
                                    .with(authentication(auth))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(createRequest(GOOGLE))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.shortCode").isNotEmpty())
                    .andExpect(jsonPath("$.originalUrl").value(GOOGLE));

            assertThat(urlRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("should fail when unauthenticated")
        void shouldFailWithoutAuth() throws Exception {

            mockMvc.perform(
                            post(URLS)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(createRequest(GOOGLE))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should fail for invalid request")
        void shouldFailForInvalidRequest() throws Exception {

            mockMvc.perform(
                            post(URLS)
                                    .with(authentication(auth))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /urls")
    class GetShortUrls {

        @Test
        @DisplayName("should return all urls for user")
        void shouldGetAllUrls() throws Exception {

            shortUrlFactory.forUser(testUser).count(2).createMany();

            mockMvc.perform(get(URLS).with(authentication(auth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("should return empty list when no urls")
        void shouldReturnEmptyList() throws Exception {

            mockMvc.perform(get(URLS).with(authentication(auth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("should fail when unauthenticated")
        void shouldFailWithoutAuth() throws Exception {
            mockMvc.perform(get(URLS))
                    .andExpect(status().isUnauthorized())
                    .andDo(
                            result -> {
                                if (result.getResolvedException() != null) {
                                    result.getResolvedException().printStackTrace();
                                }
                            });
        }
    }

    @Nested
    @DisplayName("GET /urls/{code}")
    class GetByCode {

        @Test
        @DisplayName("should return url by code")
        void shouldGetByCode() throws Exception {

            ShortUrl shortUrl = shortUrlFactory.forUser(testUser).create();

            mockMvc.perform(get(URL_BY_CODE, shortUrl.getShortCode()).with(authentication(auth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.originalUrl").value(shortUrl.getOriginalUrl()));
        }

        @Test
        @DisplayName("should return 404 if code not found")
        void shouldReturn404() throws Exception {

            mockMvc.perform(get(URL_BY_CODE, "notfound").with(authentication(auth)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /urls/{code}")
    class UpdateShortUrl {

        @Test
        @DisplayName("should update url")
        void shouldUpdate() throws Exception {

            ShortUrl shortUrl = shortUrlFactory.forUser(testUser).create();

            mockMvc.perform(
                            put(URL_BY_CODE, shortUrl.getShortCode())
                                    .with(authentication(auth))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    updateRequest(NEW_URL))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.originalUrl").value(NEW_URL));
        }

        @Test
        @DisplayName("should return 403 when updating others url")
        void shouldFailForOtherUser() throws Exception {

            ShortUrl shortUrl = shortUrlFactory.forRandomUser().create();

            mockMvc.perform(
                            put(URL_BY_CODE, shortUrl.getShortCode())
                                    .with(authentication(auth))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    updateRequest(NEW_URL))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /urls/{code}")
    class DeleteShortUrl {

        @Test
        @DisplayName("should delete url")
        void shouldDelete() throws Exception {

            ShortUrl shortUrl = shortUrlFactory.forUser(testUser).create();

            mockMvc.perform(delete(URL_BY_CODE, shortUrl.getShortCode()).with(authentication(auth)))
                    .andExpect(status().isNoContent());

            assertThat(urlRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("should fail when deleting others url")
        void shouldFailForOtherUser() throws Exception {

            ShortUrl shortUrl = shortUrlFactory.forRandomUser().create();

            mockMvc.perform(delete(URL_BY_CODE, shortUrl.getShortCode()).with(authentication(auth)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /urls/{code}/qr")
    class QrCode {

        @Test
        @DisplayName("should return QR code")
        void shouldReturnQrCode() throws Exception {

            ShortUrl shortUrl = shortUrlFactory.forUser(testUser).create();

            mockMvc.perform(get(URL_QR, shortUrl.getShortCode()).with(authentication(auth)))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "image/png"));
        }
    }
}

package io.velan.urlshortener.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.velan.urlshortener.clients.QrClient;
import io.velan.urlshortener.configs.AppProperties;
import io.velan.urlshortener.dtos.CreateShortUrlRequest;
import io.velan.urlshortener.dtos.ShortUrlResponse;
import io.velan.urlshortener.dtos.UpdateShortUrlRequest;
import io.velan.urlshortener.entities.ShortUrl;
import io.velan.urlshortener.entities.User;
import io.velan.urlshortener.exceptions.ExternalServiceException;
import io.velan.urlshortener.exceptions.UrlNotFoundException;
import io.velan.urlshortener.exceptions.UserNotFoundException;
import io.velan.urlshortener.repository.UrlRepository;
import io.velan.urlshortener.repository.UserRepository;
import io.velan.urlshortener.utils.ShortCodeGenerator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;
    private final ShortCodeGenerator shortCodeGenerator;
    private final QrClient qrClient;

    public UrlService(
            UrlRepository urlRepository,
            UserRepository userRepository,
            AppProperties appProperties,
            ShortCodeGenerator shortCodeGenerator,
            QrClient qrClient) {
        this.urlRepository = urlRepository;
        this.userRepository = userRepository;
        this.appProperties = appProperties;
        this.shortCodeGenerator = shortCodeGenerator;
        this.qrClient = qrClient;
    }

    @Transactional
    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request, Long userId) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new UserNotFoundException(userId));

        String shortCode = generateUniqueCode();
        ShortUrl url = new ShortUrl(shortCode, request.getOriginalUrl(), user);
        urlRepository.save(url);
        return toResponse(url);
    }

    @Transactional(readOnly = true)
    public ShortUrlResponse getShortUrl(String shortCode, Long userId) {
        ShortUrl url = getUrlOrThrow(shortCode);

        if (!url.isOwnedBy(userId)) {
            throw new AccessDeniedException("You do not own this URL");
        }

        return toResponse(url);
    }

    @Transactional(readOnly = true)
    public List<ShortUrlResponse> getAllShortUrls(Long userId, boolean isAdmin) {
        if (isAdmin) {
            return urlRepository.findAll().stream().map(this::toResponse).toList();
        }

        return urlRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ShortUrlResponse updateShortUrl(
            String shortCode, UpdateShortUrlRequest request, Long userId) {
        ShortUrl url = getUrlOrThrow(shortCode);

        if (!url.isOwnedBy(userId)) {
            throw new AccessDeniedException("You do not own this URL");
        }

        url.updateOriginalUrl(request.getOriginalUrl());
        urlRepository.save(url);
        return toResponse(url);
    }

    @Transactional
    public void deleteShortUrl(String shortCode, Long userId, boolean isAdmin) {
        ShortUrl url = getUrlOrThrow(shortCode);

        if (!isAdmin && !url.isOwnedBy(userId)) {
            throw new AccessDeniedException("You do not own this URL");
        }

        urlRepository.delete(url);
    }

    @Transactional(readOnly = true)
    public String getOriginalUrl(String shortCode) {
        return urlRepository
                .findByShortCode(shortCode)
                .map(ShortUrl::getOriginalUrl)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
    }

    @CircuitBreaker(name = "qr-client", fallbackMethod = "qrFallback")
    @Retry(name = "qr-client")
    public byte[] generateQrCode(String shortCode) {
        String shortUrl = appProperties.getBaseUrl() + "/" + shortCode;
        return qrClient.generateQr("150x150", shortUrl);
    }

    public byte[] qrFallback(String _shortCode, Throwable ex) {
        log.info("Fallback triggered");
        throw new ExternalServiceException("QR service unavailable");
    }

    private ShortUrlResponse toResponse(ShortUrl url) {
        String shortUrl = appProperties.getBaseUrl() + "/" + url.getShortCode();
        return new ShortUrlResponse(url.getShortCode(), shortUrl, url.getOriginalUrl());
    }

    private ShortUrl getUrlOrThrow(String shortCode) {
        return urlRepository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = shortCodeGenerator.generate();
        } while (urlRepository.existsByShortCode(code));
        return code;
    }
}

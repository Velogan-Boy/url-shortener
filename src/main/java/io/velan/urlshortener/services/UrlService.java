package io.velan.urlshortener.services;

import io.velan.urlshortener.configs.AppProperties;
import io.velan.urlshortener.dtos.CreateShortUrlRequest;
import io.velan.urlshortener.dtos.ShortUrlResponse;
import io.velan.urlshortener.dtos.UpdateShortUrlRequest;
import io.velan.urlshortener.entities.ShortUrl;
import io.velan.urlshortener.exceptions.UrlNotFoundException;
import io.velan.urlshortener.repository.UrlRepository;
import io.velan.urlshortener.utils.ShortCodeGenerator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final AppProperties appProperties;
    private final ShortCodeGenerator shortCodeGenerator;

    public UrlService(
            UrlRepository urlRepository,
            AppProperties appProperties,
            ShortCodeGenerator shortCodeGenerator) {
        this.urlRepository = urlRepository;
        this.appProperties = appProperties;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request) {
        String shortCode = shortCodeGenerator.generate();
        ShortUrl url =
                new ShortUrl(shortCode, request.getOriginalUrl(), System.currentTimeMillis());
        urlRepository.save(url);
        return toResponse(url);
    }

    public ShortUrlResponse getShortUrl(String shortCode) {
        ShortUrl url =
                urlRepository
                        .findByShortCode(shortCode)
                        .orElseThrow(() -> new UrlNotFoundException(shortCode));

        return toResponse(url);
    }

    public List<ShortUrlResponse> getAllShortUrls() {
        return urlRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ShortUrlResponse updateShortUrl(String shortCode, UpdateShortUrlRequest request) {
        ShortUrl url =
                urlRepository
                        .findByShortCode(shortCode)
                        .orElseThrow(() -> new UrlNotFoundException(shortCode));
        url.updateOriginalUrl(request.getOriginalUrl());
        urlRepository.save(url);
        return toResponse(url);
    }

    public void deleteShortUrl(String shortCode) {
        if (!urlRepository.existsByShortCode(shortCode)) {
            throw new UrlNotFoundException(shortCode);
        }
        urlRepository.deleteByShortCode(shortCode);
    }

    public String getOriginalUrl(String shortCode) {
        return urlRepository
                .findByShortCode(shortCode)
                .map(ShortUrl::getOriginalUrl)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
    }

    private ShortUrlResponse toResponse(ShortUrl url) {
        String shortUrl = appProperties.getBaseUrl() + "/" + url.getShortCode();
        return new ShortUrlResponse(url.getShortCode(), shortUrl, url.getOriginalUrl());
    }
}

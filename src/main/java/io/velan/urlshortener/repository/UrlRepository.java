package io.velan.urlshortener.repository;

import io.velan.urlshortener.entities.ShortUrl;
import java.util.List;
import java.util.Optional;

// DI Principle
public interface UrlRepository {

    void save(ShortUrl url);

    Optional<ShortUrl> findByShortCode(String shortCode);

    List<ShortUrl> findAll();

    boolean existsByShortCode(String shortCode);

    void deleteByShortCode(String shortCode);
}

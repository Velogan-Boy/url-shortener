package io.velan.urlshortener.repository;

import io.velan.urlshortener.entities.ShortUrl;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    List<ShortUrl> findByOriginalUrl(String originalUrl);

    void deleteByShortCode(String shortCode);

    List<ShortUrl> findByUserId(Long userId);
}

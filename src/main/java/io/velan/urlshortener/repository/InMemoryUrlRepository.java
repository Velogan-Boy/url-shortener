package io.velan.urlshortener.repository;

import io.velan.urlshortener.entities.ShortUrl;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryUrlRepository implements UrlRepository {

      private final Map<String, ShortUrl> store = new HashMap<>(); // TODO: DB

      @Override
      public void save(ShortUrl url) {
            store.put(url.getShortCode(), url);
      }

      @Override
      public Optional<ShortUrl> findByShortCode(String shortCode) {
            return Optional.ofNullable(store.get(shortCode));
      }

      @Override
      public List<ShortUrl> findAll() {
            return new ArrayList<>(store.values());
      }

      @Override
      public boolean existsByShortCode(String shortCode) {
            return store.containsKey(shortCode);
      }

      @Override
      public void deleteByShortCode(String shortCode) {
            store.remove(shortCode);
      }
}
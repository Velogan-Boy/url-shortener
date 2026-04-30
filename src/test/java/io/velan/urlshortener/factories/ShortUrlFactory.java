package io.velan.urlshortener.factories;

import io.velan.urlshortener.entities.ShortUrl;
import io.velan.urlshortener.entities.User;
import io.velan.urlshortener.repository.UrlRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import net.datafaker.Faker;

public class ShortUrlFactory {

    private final UrlRepository repo;
    private final UserFactory userFactory;
    private static final Faker faker = new Faker();

    public ShortUrlFactory(UrlRepository repo, UserFactory userFactory) {
        this.repo = repo;
        this.userFactory = userFactory;
    }

    public Builder builder() {
        return new Builder(repo, userFactory);
    }

    public Builder forUser(User user) {
        return builder().forUser(user);
    }

    public Builder forRandomUser() {
        return builder().forRandomUser();
    }

    public static class Builder {

        private final UrlRepository repo;
        private final UserFactory userFactory;

        private String shortCode = randomCode();
        private String originalUrl = faker.internet().url();
        private User user;
        private int count = 1;

        public Builder(UrlRepository repo, UserFactory userFactory) {
            this.repo = repo;
            this.userFactory = userFactory;
        }

        public Builder forUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withUrl(String url) {
            this.originalUrl = url;
            return this;
        }

        public Builder withCode(String code) {
            this.shortCode = code;
            return this;
        }

        public Builder count(int count) {
            this.count = count;
            return this;
        }

        public Builder forRandomUser() {
            this.user = this.userFactory.create();
            return this;
        }

        public ShortUrl build() {
            if (user == null) {
                throw new IllegalStateException("User must be provided");
            }

            return new ShortUrl(shortCode, originalUrl, user);
        }

        public List<ShortUrl> buildMany() {
            return IntStream.range(0, count)
                    .mapToObj(i -> new ShortUrl(randomCode(), faker.internet().url(), user))
                    .toList();
        }

        public ShortUrl create() {
            return repo.save(build());
        }

        public List<ShortUrl> createMany() {
            return repo.saveAll(buildMany());
        }

        private static String randomCode() {
            return UUID.randomUUID().toString().substring(0, 6);
        }
    }
}

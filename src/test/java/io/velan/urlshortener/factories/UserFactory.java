package io.velan.urlshortener.factories;

import io.velan.urlshortener.entities.User;
import io.velan.urlshortener.enums.Role;
import io.velan.urlshortener.repository.UserRepository;
import net.datafaker.Faker;

public class UserFactory {

    private final UserRepository repo;
    private static final Faker faker = new Faker();

    public UserFactory(UserRepository repo) {
        this.repo = repo;
    }

    public Builder builder() {
        return new Builder(repo);
    }

    public User create() {
        return builder().create();
    }

    public static class Builder {

        private final UserRepository repo;

        private final String username = faker.internet().username() + "_" + System.nanoTime();
        private String email = faker.internet().emailAddress();
        private final String password = "password";
        private Role role = Role.USER;

        public Builder(UserRepository repo) {
            this.repo = repo;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withRole(Role role) {
            this.role = role;
            return this;
        }

        public User build() {
            return new User(username, email, password, role);
        }

        public User create() {
            return repo.save(build());
        }
    }
}

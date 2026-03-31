package io.velan.urlshortener.utils;

import org.springframework.stereotype.Component;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ShortCodeGenerator {

      private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
      private static final int CODE_LENGTH = 6;

      public String generate() {
            StringBuilder code = new StringBuilder(CODE_LENGTH);

            for (int i = 0; i < CODE_LENGTH; i++) {
                  code.append(CHARACTERS.charAt(ThreadLocalRandom.current().nextInt(CHARACTERS.length())));
            }

            return code.toString();
      }
}
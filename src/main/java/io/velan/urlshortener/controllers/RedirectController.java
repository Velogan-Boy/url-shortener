package io.velan.urlshortener.controllers;

import io.velan.urlshortener.services.UrlService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// @RestController
// public class RedirectController {
// ..... field injection using autowired (not recommeded - hard to test, hidden dependency, break
// immutatibility).....
//     @Autowired
//     private final UrlService urlService;
//     @GetMapping("/{code}")
//     public void redirect(@PathVariable String code, HttpServletResponse response) throws
// IOException {
//         String originalUrl = urlService.getOriginalUrl(code);
//         response.sendRedirect(originalUrl);
//     }
// }
// @RestController
// public class RedirectController {
//     private final UrlService urlService;
// ...... setter injection (used for optional dependency)
//     @Autowired
//     public void setUrlService(UrlService urlService) {
//         this.urlService = urlService;
//     }
//     @GetMapping("/{code}")
//     public void redirect(@PathVariable String code, HttpServletResponse response) throws
// IOException {
//         String originalUrl = urlService.getOriginalUrl(code);
//         response.sendRedirect(originalUrl);
//     }
// }
@RestController
public class RedirectController {

    private final UrlService urlService;

    // constructor based dependency injection (no need for @Autowired as spring
    // injects it
    // automatically) RECOMMENDED !!
    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/{code}")
    public void redirect(@PathVariable("code") String code, HttpServletResponse response)
            throws IOException {
        String originalUrl = urlService.getOriginalUrl(code);
        response.sendRedirect(originalUrl);
    }
}

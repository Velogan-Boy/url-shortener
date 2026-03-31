package io.velan.urlshortener.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.velan.urlshortener.dtos.CreateShortUrlRequest;
import io.velan.urlshortener.dtos.ShortUrlResponse;
import io.velan.urlshortener.dtos.UpdateShortUrlRequest;
import io.velan.urlshortener.services.UrlService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/urls")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShortUrlResponse createShortUrl(@Valid @RequestBody CreateShortUrlRequest request) {
        return urlService.createShortUrl(request);
    }

    @GetMapping
    public List<ShortUrlResponse> getAllShortUrls() {
        return urlService.getAllShortUrls();
    }

    @GetMapping("/{code}")
    public ShortUrlResponse getShortUrl(@PathVariable("code") String code) {
        return urlService.getShortUrl(code);
    }

    @PutMapping("/{code}")
    public ShortUrlResponse updateShortUrl(@PathVariable("code") String code,
            @Valid @RequestBody UpdateShortUrlRequest request) {
        return urlService.updateShortUrl(code, request);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShortUrl(@PathVariable("code") String code) {
        urlService.deleteShortUrl(code);
    }

}

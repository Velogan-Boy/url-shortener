package io.velan.urlshortener.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.velan.urlshortener.dtos.CreateShortUrlRequest;
import io.velan.urlshortener.dtos.ShortUrlResponse;
import io.velan.urlshortener.dtos.UpdateShortUrlRequest;
import io.velan.urlshortener.services.UrlService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/urls")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @Operation(summary = "Create a short URL")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShortUrlResponse createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return urlService.createShortUrl(request, userId);
    }

    @Operation(summary = "Get all short URLs")
    @GetMapping
    public List<ShortUrlResponse> getAllShortUrls(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = isAdmin(authentication);
        return urlService.getAllShortUrls(userId, isAdmin);
    }

    @Operation(summary = "Get a short URL by its code")
    @GetMapping("/{code}")
    public ShortUrlResponse getShortUrl(
            @PathVariable("code") String code, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return urlService.getShortUrl(code, userId);
    }

    @Operation(summary = "Update a short URL")
    @PutMapping("/{code}")
    public ShortUrlResponse updateShortUrl(
            @PathVariable("code") String code,
            @Valid @RequestBody UpdateShortUrlRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return urlService.updateShortUrl(code, request, userId);
    }

    @Operation(summary = "Delete a short URL")
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShortUrl(@PathVariable("code") String code, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = isAdmin(authentication);
        urlService.deleteShortUrl(code, userId, isAdmin);
    }

    @Operation(summary = "Get QR code for a short URL")
    @GetMapping("/{code}/qr")
    public ResponseEntity<byte[]> getQr(@PathVariable("code") String code) {

        byte[] qr = urlService.generateQrCode(code);

        return ResponseEntity.ok().header("Content-Type", "image/png").body(qr);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}

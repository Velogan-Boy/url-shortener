package io.velan.urlshortener.clients;

import io.velan.urlshortener.configs.QrClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "qr-client",
        url = "https://api.qrserver.com",
        // url = "http://invalid-qr-service-url",
        configuration = QrClientConfig.class)
public interface QrClient {

    @GetMapping(value = "/v1/create-qr-code/", produces = "image/png")
    byte[] generateQr(@RequestParam("size") String size, @RequestParam("data") String data);
}

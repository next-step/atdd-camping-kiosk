package com.camping.kiosk.external.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminClient {

    private final RestTemplate restTemplate;
    private final AdminAuthClient adminAuthClient;

    @Value("${kiosk.admin.base-url}")
    private String adminBaseUrl;

    public List<Product> getProducts() {
        try {
            HttpHeaders headers = authHeaders();
            ResponseEntity<Product[]> response = restTemplate.exchange(adminBaseUrl + "/admin/products", HttpMethod.GET, new HttpEntity<>(headers), Product[].class);
            Product[] body = response.getBody();
            return body != null ? List.of(body) : List.of();
        } catch (Exception e) {
            adminAuthClient.clearCache();
            HttpHeaders headers = authHeaders();
            ResponseEntity<Product[]> response = restTemplate.exchange(adminBaseUrl + "/admin/products", HttpMethod.GET, new HttpEntity<>(headers), Product[].class);
            Product[] body = response.getBody();
            return body != null ? List.of(body) : List.of();
        }
    }

    public void confirmSale(SaleRequest request) {
        try {
            HttpHeaders headers = authHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SaleRequest> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(adminBaseUrl + "/api/sales", HttpMethod.POST, entity, Void.class);
        } catch (Exception e) {
            adminAuthClient.clearCache();
            HttpHeaders headers = authHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SaleRequest> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(adminBaseUrl + "/api/sales", HttpMethod.POST, entity, Void.class);
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String cookie = adminAuthClient.getCookieHeader();
        if (cookie != null && !cookie.isEmpty()) {
            headers.set(HttpHeaders.COOKIE, cookie);
        }
        return headers;
    }
}



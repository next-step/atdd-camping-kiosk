package com.camping.kiosk.external.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAuthClient {

    private final RestTemplate restTemplate;

    @Value("${kiosk.admin.base-url}")
    private String adminBaseUrl;

    @Value("${kiosk.admin.auth.username:admin}")
    private String username;

    @Value("${kiosk.admin.auth.password:admin123}")
    private String password;

    @Value("${kiosk.admin.auth.login-path}")
    private String loginPath;

    @Value("${kiosk.admin.auth.cookie-name:AUTH_TOKEN}")
    private String authCookieName;

    private volatile String cachedToken;
    private volatile String cookieHeader;

    public synchronized String getToken() {
        if (cachedToken == null) {
            cachedToken = login();
        }
        return cachedToken;
    }

    public synchronized String getCookieHeader() {
        if (cookieHeader == null) {
            // ensure login executed
            getToken();
        }
        return cookieHeader;
    }

    public void clearCache() {
        cachedToken = null;
        cookieHeader = null;
    }

    private String login() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> entity = new HttpEntity<>(LoginRequest.builder()
                .username(username)
                .password(password)
                .build(), headers);
        ResponseEntity<Void> response = restTemplate.exchange(adminBaseUrl + loginPath, HttpMethod.POST, entity, Void.class);
        // Capture Set-Cookie(s): support multiple cookies and cookie-name configuration
        List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (setCookies != null && !setCookies.isEmpty()) {
            // Find target cookie among multiple Set-Cookie headers (avoid comma-joined parsing pitfalls)
            String found = null;
            String prefix = authCookieName + "=";
            for (String sc : setCookies) {
                if (sc == null) continue;
                int semi = sc.indexOf(';');
                String candidate = semi > 0 ? sc.substring(0, semi) : sc; // name=value
                if (candidate.startsWith(prefix)) {
                    found = candidate;
                    break;
                }
            }
            if (found == null) {
                // Fallback: if only one cookie and no exact name match, use the first one as-is
                String first = setCookies.get(0);
                int semi = first != null ? first.indexOf(';') : -1;
                found = first != null ? (semi > 0 ? first.substring(0, semi) : first) : null;
            }
            cookieHeader = found;
            if (cookieHeader != null && cookieHeader.startsWith(prefix)) {
                cachedToken = cookieHeader.substring(prefix.length());
            } else {
                cachedToken = null; // we rely on cookie authentication; token may be unused
            }
        }
        if (cookieHeader == null) {
            throw new IllegalStateException("Admin login failed: missing cookie");
        }
        return cachedToken;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginResponse {
        private String token;
    }
}



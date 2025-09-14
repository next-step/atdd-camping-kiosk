package com.camping.kiosk.external.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentClient {

    private final RestTemplate restTemplate;

    @Value("${kiosk.payment.base-url}")
    private String paymentBaseUrl;

    @Value("${kiosk.payment.secret-key}")
    private String paymentSecretKey;

    // create 단계: 결제 트랜잭션 생성
    public CreateResponse create(PaymentRequest request) {
        String paymentKey = "pay_" + UUID.randomUUID();
        String orderId = "ord_" + UUID.randomUUID();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", buildBasicAuthHeader(paymentSecretKey));

        String createUrl = paymentBaseUrl + "/v1/payments";
        CreateRequest createBody = new CreateRequest(paymentKey, orderId, request.getAmount());
        try {
            log.info("[PaymentCreate] url={}, orderId={}, amount={}", createUrl, orderId, request.getAmount());
            ResponseEntity<CreateResponse> createRes = restTemplate.exchange(createUrl, HttpMethod.POST, new HttpEntity<>(createBody, headers), CreateResponse.class);
            CreateResponse body = createRes.getBody();
            log.info("[PaymentCreate] statusCode={}, status={}", createRes.getStatusCode(), body != null ? body.getStatus() : null);
            return body != null ? body : CreateResponse.builder().paymentKey(paymentKey).orderId(orderId).status(null).build();
        } catch (HttpStatusCodeException e) {
            log.error("[PaymentCreate][HTTP_ERROR] url={}, status={}, body={}", createUrl, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    // confirm 단계: 결제 승인 확정
    public ConfirmResponse confirm(String paymentKey, String orderId, int amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", buildBasicAuthHeader(paymentSecretKey));

        String confirmUrl = paymentBaseUrl + "/v1/payments/confirm";
        ConfirmRequest confirmBody = new ConfirmRequest(paymentKey, orderId, amount);
        try {
            log.info("[PaymentConfirm] url={}, orderId={}, amount={}", confirmUrl, orderId, amount);
            ResponseEntity<ConfirmResponse> confirmRes = restTemplate.exchange(confirmUrl, HttpMethod.POST, new HttpEntity<>(confirmBody, headers), ConfirmResponse.class);
            ConfirmResponse body = confirmRes.getBody();
            log.info("[PaymentConfirm] statusCode={}, providerStatus={}", confirmRes.getStatusCode(), body != null ? body.getStatus() : null);
            return body;
        } catch (HttpStatusCodeException e) {
            log.error("[PaymentConfirm][HTTP_ERROR] url={}, status={}, body={}", confirmUrl, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("[PaymentConfirm][ERROR] url={}, message={}", confirmUrl, e.getMessage(), e);
            throw e;
        }
    }

    public RefundResponse refund(RefundRequest request) {
        String url = paymentBaseUrl + "/v1/payments/" + request.getPaymentKey() + "/cancel";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", buildBasicAuthHeader(paymentSecretKey));
        CancelRequest body = new CancelRequest(request.getCancelReason(), request.getCancelAmount());
        HttpEntity<CancelRequest> entity = new HttpEntity<>(body, headers);
        try {
            log.info("[PaymentRefund] url={}, paymentKey={}, amount={}", url, request.getPaymentKey(), request.getCancelAmount());
            ResponseEntity<RefundResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, RefundResponse.class);
            RefundResponse resBody = response.getBody();
            log.info("[PaymentRefund] statusCode={}, providerStatus={}", response.getStatusCode(), resBody != null ? resBody.getStatus() : null);
            return resBody;
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("[PaymentRefund][HTTP_ERROR] url={}, status={}, body={}", url, e.getStatusCode(), responseBody);
            throw e;
        } catch (Exception e) {
            log.error("[PaymentRefund][ERROR] url={}, message={}", url, e.getMessage(), e);
            throw e;
        }
    }

    private String buildBasicAuthHeader(String secretKey) {
        String token = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentRequest {
        private int amount;
        private String method;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentResponse {
        private String transactionId;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String paymentKey;
        private String orderId;
        private Integer amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateResponse {
        private String paymentKey;
        private String orderId;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfirmRequest {
        private String paymentKey;
        private String orderId;
        private Integer amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfirmResponse {
        private String paymentKey;
        private String orderId;
        private String method;
        private String approvedAt;
        private Integer totalAmount;
        private String status;
        private Receipt receipt;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Receipt {
            private String url;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefundRequest {
        private String paymentKey;
        private String cancelReason;
        private Integer cancelAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefundResponse {
        private String paymentKey;
        private String status;
        private String canceledAt;
    }

    

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class CancelRequest {
        private String cancelReason;
        private Integer cancelAmount;
    }
}



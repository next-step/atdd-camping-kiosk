package com.camping.kiosk.web;

import com.camping.kiosk.external.payment.dto.PaymentConfirmResponse;
import com.camping.kiosk.external.payment.dto.PaymentConfirmRequest;
import com.camping.kiosk.external.payment.dto.PaymentCreateRequest;
import com.camping.kiosk.external.payment.dto.PaymentCreateResult;
import com.camping.kiosk.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/payments")
    public ResponseEntity<PaymentCreateResult> createPayment(@RequestBody PaymentCreateRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    @PostMapping("/api/payments/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(request));
    }
}



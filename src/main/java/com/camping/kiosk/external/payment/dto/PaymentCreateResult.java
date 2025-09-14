package com.camping.kiosk.external.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateResult {
    private boolean success;
    private String message;
    private String paymentKey;
    private String orderId;
    private int amount;
}



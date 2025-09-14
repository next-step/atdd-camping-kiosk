package com.camping.kiosk.external.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmResponse {
    private boolean success;
    private String transactionId;
    private String message;
    private int paidAmount;
}



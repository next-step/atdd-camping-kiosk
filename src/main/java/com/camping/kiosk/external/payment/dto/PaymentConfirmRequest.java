package com.camping.kiosk.external.payment.dto;

import com.camping.kiosk.domain.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmRequest {
    private String paymentKey;
    private String orderId;
    private Integer amount;
    private java.util.List<CartItem> items;
}



package com.camping.kiosk.external.payment.dto;

import com.camping.kiosk.domain.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateRequest {
    private List<CartItem> items;
    private String paymentMethod; // CARD, CASH
}



package com.camping.kiosk.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    private Long productId;
    private String productName;
    private int unitPrice;
    private int quantity;

    public int getLineTotal() {
        return unitPrice * quantity;
    }
}



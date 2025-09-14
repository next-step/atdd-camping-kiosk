package com.camping.kiosk.service;

import com.camping.kiosk.domain.*;
import com.camping.kiosk.external.admin.AdminClient;
import com.camping.kiosk.external.admin.SaleItem;
import com.camping.kiosk.external.admin.Product;
import com.camping.kiosk.external.admin.SaleRequest;
import com.camping.kiosk.external.payment.PaymentClient;
import com.camping.kiosk.external.payment.dto.PaymentConfirmRequest;
import com.camping.kiosk.external.payment.dto.PaymentConfirmResponse;
import com.camping.kiosk.external.payment.dto.PaymentCreateRequest;
import com.camping.kiosk.external.payment.dto.PaymentCreateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final AdminClient adminClient;
    private final PaymentClient paymentClient;

    public List<Product> loadProducts() {
        return adminClient.getProducts();
    }

    public PaymentCreateResult createPayment(PaymentCreateRequest request) {
        int amount = calculateAmount(request.getItems());
        try {
            PaymentClient.CreateResponse created = paymentClient.create(
                    PaymentClient.PaymentRequest.builder()
                            .amount(amount)
                            .method(Optional.ofNullable(request.getPaymentMethod()).orElse("CARD"))
                            .description("kiosk purchase")
                            .build());
            if (created == null || created.getPaymentKey() == null) {
                return PaymentCreateResult.builder().success(false).message("결제 생성 실패").amount(0).build();
            }
            return PaymentCreateResult.builder()
                    .success(true)
                    .message("결제 생성 성공")
                    .paymentKey(created.getPaymentKey())
                    .orderId(created.getOrderId())
                    .amount(amount)
                    .build();
        } catch (Exception e) {
            log.error("payment create error", e);
            return PaymentCreateResult.builder().success(false).message("결제 생성 실패").amount(0).build();
        }
    }

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {
        int amount = request.getAmount() != null ? request.getAmount() : calculateAmount(request.getItems());
        try {
            PaymentClient.ConfirmResponse confirmed = paymentClient.confirm(request.getPaymentKey(), request.getOrderId(), amount);
            if (confirmed == null || confirmed.getPaymentKey() == null) {
                return PaymentConfirmResponse.builder().success(false).message("결제 승인 실패").paidAmount(0).build();
            }

            confirmAdmin(request.getItems());

            return PaymentConfirmResponse.builder()
                    .success(true)
                    .transactionId(confirmed.getPaymentKey())
                    .paidAmount(amount)
                    .message("결제 성공")
                    .build();
        } catch (Exception e) {
            log.error("payment confirm error", e);
            return PaymentConfirmResponse.builder().success(false).message("결제 또는 확정 실패").paidAmount(0).build();
        }
    }

    private int calculateAmount(List<CartItem> items) {
        return items.stream().mapToInt(CartItem::getLineTotal).sum();
    }

    private void confirmAdmin(List<CartItem> items) {
        List<SaleItem> saleItems = items.stream()
                .map(i -> SaleItem.builder().productId(i.getProductId()).quantity(i.getQuantity()).build())
                .toList();
        confirmSale(SaleRequest.builder().items(saleItems).build());
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 200))
    private void confirmSale(SaleRequest request) {
        adminClient.confirmSale(request);
    }
}



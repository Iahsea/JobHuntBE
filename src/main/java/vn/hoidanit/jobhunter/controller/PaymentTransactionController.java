package vn.hoidanit.jobhunter.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.PaymentTransaction;
import vn.hoidanit.jobhunter.domain.Subscription;
import vn.hoidanit.jobhunter.domain.request.SepayQrRequest;
import vn.hoidanit.jobhunter.domain.request.SepayWebhookDto;
import vn.hoidanit.jobhunter.domain.response.PaymentStatusResponse;
import vn.hoidanit.jobhunter.domain.response.SepayQrResponse;
import vn.hoidanit.jobhunter.service.PaymentTransactionService;
import vn.hoidanit.jobhunter.service.SubscriptionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class PaymentTransactionController {

    private final PaymentTransactionService paymentTransactionService;
    private final SubscriptionService subscriptionService;

    public PaymentTransactionController(
            PaymentTransactionService paymentTransactionService,
            SubscriptionService subscriptionService) {
        this.paymentTransactionService = paymentTransactionService;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Endpoint để user mua gói subscription và nhận QR code thanh toán
     */
    @PostMapping("/subscriptions/purchase")
    public ResponseEntity<SepayQrResponse> purchaseSubscription(
            @RequestBody SepayQrRequest request) {

        Long userId = request.getUserId();
        Long planId = request.getPlanId();

        if (userId == null || planId == null) {
            throw new IllegalArgumentException("userId and planId are required");
        }

        log.info("User {} purchasing plan {}", userId, planId);

        //  Tạo subscription với trạng thái PENDING_PAYMENT
        Subscription sub = subscriptionService.createPendingSubscription(userId, planId);

        //  Tạo payment transaction với trạng thái PENDING
        PaymentTransaction tx = paymentTransactionService.createPendingTransaction(sub);

        //  Tạo QR code Sepay
        SepayQrResponse qrResponse = paymentTransactionService.createQR(tx);

        log.info("Created QR payment for transaction {}", tx.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(qrResponse);
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/payments/transactions/{transactionId}/status")
    public ResponseEntity<PaymentStatusResponse> checkPaymentStatus(
            @PathVariable Long transactionId) {

        PaymentTransaction tx = paymentTransactionService.getTransactionById(transactionId);

        return ResponseEntity.ok(
                new PaymentStatusResponse(
                        tx.getId(),
                        tx.getStatus().name(),
                        tx.getAmount(),
                        tx.getProvider(),
                        tx.getCreatedAt(),
                        tx.getPaidAt()
                )
        );
    }
    /**
     * Lấy lịch sử giao dịch của user
     */
    @GetMapping("/users/{userId}/payments")
    public ResponseEntity<List<PaymentTransaction>> getUserPayments(
            @PathVariable Long userId) {

        List<PaymentTransaction> transactions = paymentTransactionService.getTransactionsByUserId(userId);

        return ResponseEntity.ok(transactions);
    }

    /**
     * Webhook từ Sepay khi có giao dịch thành công
     */
    @PostMapping("/payments/sepay/webhook")
    public ResponseEntity<Void> webhook(@RequestBody SepayWebhookDto dto) {

        paymentTransactionService.handleSepayWebhook(dto);

        return ResponseEntity.ok().build();
    }

}

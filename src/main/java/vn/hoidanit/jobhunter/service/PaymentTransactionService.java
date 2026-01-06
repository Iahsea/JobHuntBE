package vn.hoidanit.jobhunter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.jobhunter.config.SepayQrProperties;
import vn.hoidanit.jobhunter.domain.PaymentTransaction;
import vn.hoidanit.jobhunter.domain.Subscription;
import vn.hoidanit.jobhunter.domain.request.SepayWebhookDto;
import vn.hoidanit.jobhunter.domain.response.SepayQrResponse;
import vn.hoidanit.jobhunter.repository.PaymentTransactionRepository;
import vn.hoidanit.jobhunter.util.constant.PaymentStatus;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final SepayQrProperties sepayQrProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SubscriptionService subscriptionService;

    public PaymentTransactionService(PaymentTransactionRepository paymentTransactionRepository,
            SepayQrProperties sepayQrProperties, SubscriptionService subscriptionService) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.sepayQrProperties = sepayQrProperties;
        this.subscriptionService = subscriptionService;
    }

    @Transactional
    public PaymentTransaction createPendingTransaction(Subscription sub) {

        log.info("Creating pending transaction for subscription {}, plan {}, amount {}",
                sub.getId(), sub.getPlan().getCode(), sub.getPlan().getPrice());

        PaymentTransaction tx = PaymentTransaction.builder()
                .subscription(sub)
                .provider("SEPAY")
                .amount(sub.getPlan().getPrice())
                .status(PaymentStatus.PENDING)
                .build();

        PaymentTransaction saved = paymentTransactionRepository.save(tx);
        log.info("Created transaction {} with status PENDING", saved.getId());

        return saved;
    }

    public SepayQrResponse createQR(PaymentTransaction tx) {
        if (tx.getId() == null) {
            throw new IllegalArgumentException("Transaction must be saved before creating QR");
        }

        String invoiceNo = "SUB_" + tx.getId();
        String qrUrl = createSepayQrLink(invoiceNo, tx.getAmount());

        log.info("Generated QR code for transaction {}, invoice {}", tx.getId(), invoiceNo);

        return new SepayQrResponse(
                tx.getId(),
                qrUrl,
                tx.getAmount(),
                "Thanh toan goi " + tx.getSubscription().getPlan().getName());
    }

    public String createSepayQrLink(String invoiceNo, Long amount) {

        String description = URLEncoder.encode(
                "Thanh toan " + invoiceNo,
                StandardCharsets.UTF_8);
        return "https://qr.sepay.vn/img"
                + "?acc=" + sepayQrProperties.getAccountNumber()
                + "&bank=" + sepayQrProperties.getBank()
                + "&amount=" + amount
                + "&des=" + description;
    }

    public Long extractTransactionId(String description) {
        if (description == null) {
            return null;
        }


        Pattern pattern = Pattern.compile("SUB[_ ]?(\\d+)");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return Long.valueOf(matcher.group(1));
        }

        return null;
    }

    public void verifyWebhookBusiness(
            PaymentTransaction tx,
            long paidAmount) {

        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("Transaction {} already processed, ignoring duplicate webhook", tx.getId());
            throw new IllegalStateException("Transaction already processed");
        }

        if (!tx.getAmount().equals(paidAmount)) {
            log.error("Amount mismatch for transaction {}: expected={}, paid={}",
                    tx.getId(), tx.getAmount(), paidAmount);
            throw new IllegalArgumentException("Paid amount mismatch");
        }
    }

    private String safeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }


    @Transactional
    public void handleSepayWebhook(SepayWebhookDto dto) {

        log.info("Processing Sepay webhook: id={}, amount={}, transferType={}, content={}",
                dto.getId(), dto.getTransferAmount(), dto.getTransferType(), dto.getContent());

        // Kiểm tra loại giao dịch (chỉ xử lý tiền vào)
        if (!"in".equalsIgnoreCase(dto.getTransferType())) {
            log.warn("Ignoring webhook with transferType: {}", dto.getTransferType());
            return; // ignore tiền ra
        }

        // Parse transactionId from content
        Long txId = extractTransactionId(dto.getContent());
        if (txId == null) {
            log.error("Cannot extract transactionId from content: {}", dto.getContent());
            throw new IllegalArgumentException("Cannot extract transactionId from content");
        }
        log.info("Extracted transaction ID: {}", txId);

        // Tìm transaction trong DB
        PaymentTransaction tx = paymentTransactionRepository
                .findById(txId)
                .orElseThrow(() -> {
                    log.error("Transaction not found in database: {}", txId);
                    return new IllegalStateException("Transaction not found: " + txId);
                });

        log.info("Found transaction in DB: id={}, status={}, amount={}",
                tx.getId(), tx.getStatus(), tx.getAmount());

        if (tx.getStatus() == PaymentStatus.EXPIRED) {
            log.warn("Received SUCCESS webhook for EXPIRED transaction {}", tx.getId());

            // option 1: bỏ qua
            return;

        }

        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            log.info("Transaction {} already SUCCESS, ignore duplicate webhook", tx.getId());
            return;
        }

        // verify business
        try {
            verifyWebhookBusiness(tx, dto.getTransferAmount());
            log.info("Business validation passed");
        } catch (Exception e) {
            log.error("Business validation failed: {}", e.getMessage());
            throw e;
        }

        // Update transaction
        tx.setStatus(PaymentStatus.SUCCESS);
        tx.setExternalRef(String.valueOf(dto.getId()));
        tx.setPaidAt(java.time.LocalDateTime.now());
        tx.setPayload(safeToJson(dto));

        paymentTransactionRepository.save(tx);
        log.info("Updated transaction {} to SUCCESS status", tx.getId());

        // Activate subscription
        try {
            subscriptionService.activateSubscriptionAndInitMonthlyUsage(tx.getSubscription().getId());
            log.info("Activated subscription {} successfully", tx.getSubscription().getId());
        } catch (Exception e) {
            log.error("Failed to activate subscription {}: {}",
                    tx.getSubscription().getId(), e.getMessage());
            throw e;
        }

    }

    public PaymentTransaction getTransactionById(Long id) {
        return paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Transaction not found: " + id));
    }

    public List<PaymentTransaction> getTransactionsByUserId(Long userId) {
        return paymentTransactionRepository.findBySubscription_User_Id(userId);
    }

}

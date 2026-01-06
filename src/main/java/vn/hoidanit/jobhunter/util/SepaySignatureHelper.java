package vn.hoidanit.jobhunter.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class để test tính toán signature cho Sepay webhook
 * Chỉ dùng cho testing/development
 */
@Slf4j
public class SepaySignatureHelper {

    public static void main(String[] args) {

        // Ví dụ test signature
        String secret = "Kx7R9mQ2ZpN4cW5FJdE8TtL3A6S0VYH1UoI";
        String transactionId = "TEST_001";
        String amount = "299000";
        String description = "Thanh toan SUB_1";
        String status = "SUCCESS";

        String signature = calculateSignature(transactionId, amount, description, status, secret);

        System.out.println("=== SEPAY WEBHOOK SIGNATURE CALCULATOR ===");
        System.out.println("Transaction ID: " + transactionId);
        System.out.println("Amount: " + amount);
        System.out.println("Description: " + description);
        System.out.println("Status: " + status);
        System.out.println("Secret: " + secret);
        System.out.println();
        System.out.println("Payload: " + transactionId + amount + description + status);
        System.out.println();
        System.out.println("Signature (Base64): " + signature);
        System.out.println();
        System.out.println("=== WEBHOOK JSON ===");
        System.out.println("{");
        System.out.println("  \"transaction_id\": \"" + transactionId + "\",");
        System.out.println("  \"amount\": " + amount + ",");
        System.out.println("  \"description\": \"" + description + "\",");
        System.out.println("  \"status\": \"" + status + "\",");
        System.out.println("  \"signature\": \"" + signature + "\"");
        System.out.println("}");
    }

    public static String calculateSignature(
            String transactionId,
            String amount,
            String description,
            String status,
            String secret) {

        try {
            // Ghép payload theo đúng thứ tự
            String payload = transactionId + amount + description + status;

            // HMAC-SHA256
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            mac.init(keySpec);

            byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // Encode to Base64
            return Base64.getEncoder().encodeToString(rawHmac);

        } catch (Exception e) {
            log.error("Error calculating signature", e);
            throw new RuntimeException("Cannot calculate signature", e);
        }
    }

    /**
     * Verify signature
     */
    public static boolean verifySignature(
            String transactionId,
            String amount,
            String description,
            String status,
            String providedSignature,
            String secret) {

        String calculatedSignature = calculateSignature(
                transactionId, amount, description, status, secret);

        return calculatedSignature.equals(providedSignature);
    }
}

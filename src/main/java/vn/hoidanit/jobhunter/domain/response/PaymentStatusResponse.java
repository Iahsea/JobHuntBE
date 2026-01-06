package vn.hoidanit.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class PaymentStatusResponse {
    private Long transactionId;
    private String status;
    private Long amount;
    private String provider;
    private Instant createdAt;
    private LocalDateTime  paidAt;
}

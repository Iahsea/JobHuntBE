package vn.hoidanit.jobhunter.domain;

import jakarta.persistence.*;
import lombok.*;
import vn.hoidanit.jobhunter.util.constant.PaymentStatus;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    // SEPAY | MOMO | ZALOPAY
    private String provider;

    private Long amount;

    // PENDING | SUCCESS | FAILED | EXPIRED
    private PaymentStatus status;

    // mã giao dịch từ cổng thanh toán
    private String externalRef;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String payload;

    private Instant createdAt = Instant.now();
    private LocalDateTime paidAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}

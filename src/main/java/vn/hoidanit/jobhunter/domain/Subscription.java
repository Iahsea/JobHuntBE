package vn.hoidanit.jobhunter.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // HR/User mua gói
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    // PENDING_PAYMENT | ACTIVE | EXPIRED | CANCELED
    private String status;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    // reset quota theo tháng
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;

    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "subscription", fetch = FetchType.LAZY)
    private List<PaymentTransaction> payments;

    @OneToMany(mappedBy = "subscription", fetch = FetchType.LAZY)
    private List<SubscriptionUsage> usages;
}

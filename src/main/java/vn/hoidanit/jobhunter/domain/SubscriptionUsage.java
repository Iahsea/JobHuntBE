package vn.hoidanit.jobhunter.domain;

import jakarta.persistence.*;
import lombok.*;
import vn.hoidanit.jobhunter.util.constant.AccessAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_usage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subscription_id", "action", "period_start"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessAction action;

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    private int usedValue = 0;
}

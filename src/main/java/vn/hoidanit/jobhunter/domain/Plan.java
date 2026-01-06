package vn.hoidanit.jobhunter.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;
    // HR_MONTH, HRVIP_YEAR, USERVIP_MONTH...

    private String name;

    private String shortDescription;

    // HR | USER
    private String audience;

    // BASIC | VIP
    private String tier;

    // MONTH | YEAR
    private String billingCycle;

    private int durationMonths; // 1 hoặc 12

    private Long price;

    private  String badge;

    private boolean isActive = true;

    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Subscription> subscriptions;

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanEntitlement> entitlements;

}

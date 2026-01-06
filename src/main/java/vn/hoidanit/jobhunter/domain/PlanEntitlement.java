package vn.hoidanit.jobhunter.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import vn.hoidanit.jobhunter.util.constant.AccessAction;

@Entity
@Table(name = "plan_entitlements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"plan_id", "action"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanEntitlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessAction action;

    // -1 unlimited, >0 quota/kỳ
    private int quotaPerPeriod;

    private int consumeUnit = 1;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    @JsonIgnore
    private Plan plan;


}

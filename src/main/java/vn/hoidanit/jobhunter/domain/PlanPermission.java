package vn.hoidanit.jobhunter.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"plan_id", "permission_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    // -1 = unlimited, >0 = quota / kỳ
    private int quotaPerPeriod;

    // mỗi lần consume bao nhiêu (thường = 1)
    private int consumeUnit = 1;
}

package vn.hoidanit.jobhunter.domain.request;



import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PlanRequest {
    private String code;          // HR_MONTH...
    private String name;
    private String audience;      // HR | USER
    private String tier;          // BASIC | VIP
    private String billingCycle;  // MONTH | YEAR
    private int durationMonths;   // 1 | 12
    private Long price;
    private Boolean isActive;
}

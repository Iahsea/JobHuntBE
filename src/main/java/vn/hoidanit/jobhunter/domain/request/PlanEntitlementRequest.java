package vn.hoidanit.jobhunter.domain.request;

import lombok.*;
import vn.hoidanit.jobhunter.util.constant.AccessAction;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PlanEntitlementRequest {
    private Long planId;
    private AccessAction action;
    private Integer quotaPerPeriod; // -1 unlimited, 10, 50...
    private Integer consumeUnit;    // default 1
}

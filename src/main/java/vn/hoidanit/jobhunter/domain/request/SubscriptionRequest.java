package vn.hoidanit.jobhunter.domain.request;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SubscriptionRequest {
    private Long userId;
    private Long planId;
    private String status; // PENDING_PAYMENT | ACTIVE | EXPIRED ...
}
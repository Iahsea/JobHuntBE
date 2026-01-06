package vn.hoidanit.jobhunter.domain.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private Long userId;
    private Long planId;
    private String planCode;
    private String status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
package vn.hoidanit.jobhunter.domain.response.access;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessResponse {
    private String status;      // OK | NO_SUBSCRIPTION | QUOTA_EXCEEDED | NOT_ALLOWED
    private String planCode;    // HR_MONTH / HRVIP_MONTH...
    private Integer remaining;  // còn lại bao nhiêu (null nếu không tính)
    private String redirect;    // /hr/pricing hoặc /pricing (gợi ý cho FE)
}
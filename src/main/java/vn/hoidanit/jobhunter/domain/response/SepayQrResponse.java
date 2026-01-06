package vn.hoidanit.jobhunter.domain.response;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SepayQrResponse {
    private Long transactionId;
    private String qrUrl;
    private Long amount;
    private String content;

}

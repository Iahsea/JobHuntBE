package vn.hoidanit.jobhunter.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SepayWebhookDto {
    @JsonProperty("id")
    private Long id; // ID giao dịch trên SePay

    @JsonProperty("gateway")
    private String gateway; // Brand name của ngân hàng (Vietcombank, ...)

    @JsonProperty("transactionDate")
    private String transactionDate; // Thời gian xảy ra giao dịch

    @JsonProperty("accountNumber")
    private String accountNumber; // Số tài khoản ngân hàng

    @JsonProperty("code")
    private String code; // Mã code thanh toán (sepay tự nhận diện dựa vào cấu hình)

    @JsonProperty("content")
    private String content; // Nội dung chuyển khoản

    @JsonProperty("transferType")
    private String transferType; // Loại giao dịch (in = tiền vào, out = tiền ra)

    @JsonProperty("transferAmount")
    private Long transferAmount; // Số tiền giao dịch

    @JsonProperty("accumulated")
    private Long accumulated; // Số dư tài khoản (lũy kế)

    @JsonProperty("subAccount")
    private String subAccount; // Tài khoản ngân hàng phụ (tài khoản định danh)

    @JsonProperty("referenceCode")
    private String referenceCode; // Mã tham chiếu của tin nhắn sms

    @JsonProperty("description")
    private String description; // Toàn bộ nội dung tin nhắn sms
}

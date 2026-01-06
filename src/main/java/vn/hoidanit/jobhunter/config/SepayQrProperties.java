package vn.hoidanit.jobhunter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "sepay.qr")
@Getter
@Setter
public class SepayQrProperties {

    private String accountNumber;
    private String bank;
    private String currency;
    private Integer expireMinutes;
    private String webhookSecret;
//    private List<String> webhookIps; // Danh sách IP được phép gọi webhook
}

package vn.hoidanit.jobhunter.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.jobhunter.config.SepayQrProperties;
import vn.hoidanit.jobhunter.repository.PaymentTransactionRepository;
import vn.hoidanit.jobhunter.util.constant.PaymentStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpireJob {

    private final PaymentTransactionRepository repo;
    private final SepayQrProperties qrProperties;

    /**
     * Chạy mỗi 1 phút để hết hạn các giao dịch pending quá lâu
     * Cron: 0
     */
//    1****=
//    Chạy vào giây 0
//    của mỗi phút*/

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void expireQrPayments() {

        Instant expiredTime = Instant.now()
                .minus(qrProperties.getExpireMinutes(), ChronoUnit.MINUTES);

        int affected = repo.expirePendingTransactions(
                PaymentStatus.PENDING,
                PaymentStatus.EXPIRED,
                expiredTime);

        if (affected > 0) {
            log.info("Expired {} pending payment(s)", affected);
        }
    }

}

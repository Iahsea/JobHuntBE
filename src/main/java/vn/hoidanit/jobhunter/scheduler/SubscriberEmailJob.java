package vn.hoidanit.jobhunter.scheduler;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.hoidanit.jobhunter.service.SubscriberService;

@Component
@RequiredArgsConstructor
public class SubscriberEmailJob {
    private final SubscriberService subscriberService;

    // chạy mỗi ngày lúc 8h sáng
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDailyJobEmails() {
        subscriberService.sendSubscribersEmailJobs();
    }
}

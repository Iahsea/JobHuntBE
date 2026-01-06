package vn.hoidanit.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.hoidanit.jobhunter.domain.SubscriptionUsage;
import vn.hoidanit.jobhunter.util.constant.AccessAction;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SubscriptionUsageRepository extends JpaRepository<SubscriptionUsage, Long> {

    @Query("""
        SELECT su FROM SubscriptionUsage su
        WHERE su.subscription.id = :subId
          AND su.action = :action
          AND su.periodStart = :periodStart
        """)
    Optional<SubscriptionUsage> findBySubAndActionAndPeriodStart(Long subId, AccessAction action, LocalDateTime periodStart);
}

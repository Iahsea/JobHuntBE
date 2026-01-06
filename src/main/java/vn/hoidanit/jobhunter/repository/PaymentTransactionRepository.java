package vn.hoidanit.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.hoidanit.jobhunter.domain.PaymentTransaction;
import vn.hoidanit.jobhunter.util.constant.PaymentStatus;

import java.time.Instant;
import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @Modifying
    @Query("""
                update PaymentTransaction p
                set p.status = :expired
                where p.status = :pending
                  and p.createdAt < :expiredTime
            """)
    int expirePendingTransactions(
            PaymentStatus pending,
            PaymentStatus expired,
            Instant expiredTime);

    List<PaymentTransaction> findBySubscription_User_Id(Long userId);
}

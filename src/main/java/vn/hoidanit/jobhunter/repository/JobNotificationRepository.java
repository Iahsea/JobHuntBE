package vn.hoidanit.jobhunter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.jobhunter.domain.JobNotification;

import java.util.List;

@Repository
public interface JobNotificationRepository extends JpaRepository<JobNotification, Long> {

    /**
     * Tìm tất cả notifications của một user
     */
    Page<JobNotification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Đếm số notifications chưa đọc của user
     */
    long countByUserIdAndReadFalse(Long userId);

    /**
     * Lấy danh sách notifications chưa đọc của user
     */
    List<JobNotification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Đánh dấu tất cả notifications của user là đã đọc
     */
    @Modifying
    @Query("UPDATE JobNotification n SET n.read = true WHERE n.user.id = :userId")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    /**
     * Kiểm tra user đã nhận notification cho job này chưa (để tránh trùng lặp)
     */
    boolean existsByUserIdAndJobId(Long userId, Long jobId);
}


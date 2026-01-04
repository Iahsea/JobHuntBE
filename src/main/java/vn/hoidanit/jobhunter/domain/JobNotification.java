package vn.hoidanit.jobhunter.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.hoidanit.jobhunter.util.SecurityUtil;

import java.time.Instant;

@Entity
@Table(name = "job_notifications")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = true)
    Job job;

    String title; // For custom notifications

    String message;

    String type; // JOB_MATCH, WELCOME, ANNOUNCEMENT, PROMOTION, etc.

    @Column(name = "is_read")
    @Builder.Default
    boolean read = false;

    Instant createdAt;

    String createdBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "system";
        this.createdAt = Instant.now();
    }
}


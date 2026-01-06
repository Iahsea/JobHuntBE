package vn.hoidanit.jobhunter.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.JobNotification;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.CustomNotificationRequest;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.JobNotificationService;
import vn.hoidanit.jobhunter.service.JobService;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.SecurityUtil;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobNotificationController {

    JobNotificationService jobNotificationService;
    UserService userService;
    JobService jobService;

    /**
     * Lấy danh sách notifications của user hiện tại
     */
    @GetMapping("/notifications")
    @ApiMessage("Fetch user notifications")
    public ResponseEntity<ResultPaginationDTO> getMyNotifications(Pageable pageable) {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        User currentUser = userService.handleGetUserByUsername(email);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Page<JobNotification> notifications = jobNotificationService.getUserNotifications(
                currentUser.getId(), pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(notifications.getTotalPages());
        mt.setTotal(notifications.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(notifications.getContent());

        return ResponseEntity.ok(rs);
    }

    /**
     * Đếm số notifications chưa đọc
     */
    @GetMapping("/notifications/unread-count")
    @ApiMessage("Count unread notifications")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        User currentUser = userService.handleGetUserByUsername(email);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        long count = jobNotificationService.countUnreadNotifications(currentUser.getId());

        Map<String, Long> result = new HashMap<>();
        result.put("unreadCount", count);

        return ResponseEntity.ok(result);
    }

    /**
     * Lấy danh sách notifications chưa đọc
     */
    @GetMapping("/notifications/unread")
    @ApiMessage("Fetch unread notifications")
    public ResponseEntity<List<JobNotification>> getUnreadNotifications() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        User currentUser = userService.handleGetUserByUsername(email);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        List<JobNotification> notifications = jobNotificationService.getUnreadNotifications(currentUser.getId());

        return ResponseEntity.ok(notifications);
    }

    /**
     * Đánh dấu một notification là đã đọc
     */
    @PutMapping("/notifications/{id}/read")
    @ApiMessage("Mark notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        jobNotificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Đánh dấu tất cả notifications là đã đọc
     */
    @PutMapping("/notifications/read-all")
    @ApiMessage("Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        User currentUser = userService.handleGetUserByUsername(email);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        jobNotificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Gửi notification thủ công cho một job (dành cho Admin/HR)
     */
    @PostMapping("/notifications/send/{jobId}")
    @ApiMessage("Send job notification manually")
    public ResponseEntity<Map<String, String>> sendJobNotification(@PathVariable Long jobId) {
        Optional<Job> jobOptional = jobService.fetchJobById(jobId);

        if (jobOptional.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Job not found with id: " + jobId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Job job = jobOptional.get();

        // Gửi notification
        jobNotificationService.notifyNewJob(job);

        Map<String, String> result = new HashMap<>();
        result.put("message", "Notifications sent successfully for job: " + job.getName());

        return ResponseEntity.ok(result);
    }

    /**
     * Gửi notification tùy chỉnh (Welcome, Announcement, etc.)
     * Dành cho Admin
     */
    @PostMapping("/notifications/custom")
    @ApiMessage("Send custom notification")
    public ResponseEntity<Map<String, Object>> sendCustomNotification(
            @RequestBody CustomNotificationRequest request) {

        Map<String, Object> result = jobNotificationService.sendCustomNotification(
            request.getTitle(),
            request.getMessage(),
            request.getType(),
            request.getUserIds()
        );

        return ResponseEntity.ok(result);
    }
}


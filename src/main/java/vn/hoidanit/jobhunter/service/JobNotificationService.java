package vn.hoidanit.jobhunter.service;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hoidanit.jobhunter.domain.*;
import vn.hoidanit.jobhunter.domain.response.job.JobNotificationDTO;
import vn.hoidanit.jobhunter.repository.JobNotificationRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JobNotificationService {

    SocketIOServer socketIOServer;
    WebSocketSessionService webSocketSessionService;
    UserRepository userRepository;
    JobNotificationRepository jobNotificationRepository;
    CompanyService companyService;

    /**
     * Gửi thông báo job mới đến các user có kỹ năng phù hợp
     *
     * @param job Job mới được tạo
     */
    @Transactional
    public void notifyNewJob(Job job) {
        try {
            // Lấy danh sách skill IDs của job
            List<Long> jobSkillIds = job.getSkills().stream()
                    .map(Skill::getId)
                    .collect(Collectors.toList());

            if (jobSkillIds.isEmpty()) {
                log.info("Job {} has no skills, skipping notification", job.getId());
                return;
            }

            // Tìm users có profile với skills phù hợp
            List<User> matchedUsers = userRepository.findUsersWithMatchingSkills(jobSkillIds);

            if (matchedUsers.isEmpty()) {
                log.info("No users found with matching skills for job {}", job.getId());
                return;
            }

            // Tạo notification DTO để gửi qua WebSocket
            JobNotificationDTO notificationDTO = buildJobNotification(job);

            // Lấy danh sách user IDs dưới dạng String
            List<String> userIds = matchedUsers.stream()
                    .map(user -> String.valueOf(user.getId()))
                    .collect(Collectors.toList());

            // Lấy tất cả socket sessions của các users này
            List<WebSocketSession> sessions = webSocketSessionService.getSessionsByUserIds(userIds);
            log.info("Found {} active WebSocket sessions for {} matched users", sessions.size(), matchedUsers.size());

            // Lưu notifications vào database và gửi qua WebSocket
            int notificationsSent = 0;
            int notificationsSaved = 0;

            for (User user : matchedUsers) {
                // Kiểm tra xem user đã nhận notification này chưa (tránh trùng)
                if (!jobNotificationRepository.existsByUserIdAndJobId(user.getId(), job.getId())) {
                    // Lưu vào database
                    JobNotification notification = JobNotification.builder()
                            .user(user)
                            .job(job)
                            .title(null) // Job notifications không cần title riêng
                            .message("Có công việc mới phù hợp với kỹ năng của bạn!")
                            .type("JOB_MATCH") // Đánh dấu là job matching notification
                            .read(false)
                            .build();
                    jobNotificationRepository.save(notification);
                    notificationsSaved++;
                }

                // Gửi qua WebSocket nếu user đang online
                String userId = String.valueOf(user.getId());
                for (WebSocketSession session : sessions) {
                    if (session.getUserId().equals(userId)) {
                        try {
                            var client = socketIOServer
                                    .getClient(java.util.UUID.fromString(session.getSocketSessionId()));
                            if (client != null) {
                                client.sendEvent("newJobNotification", notificationDTO);
                                notificationsSent++;
                            } else {
                                log.debug("Client not connected for session {}", session.getSocketSessionId());
                            }
                        } catch (Exception e) {
                            log.error("Failed to send notification to session {}: {}",
                                    session.getSocketSessionId(), e.getMessage());
                        }
                    }
                }
            }

            log.info("Job notification completed: {} saved to DB, {} sent via WebSocket for job: {} (matched {} users)",
                    notificationsSaved, notificationsSent, job.getName(), matchedUsers.size());

        } catch (Exception e) {
            log.error("Error sending job notifications for job {}: {}", job.getId(), e.getMessage(), e);
        }
    }

    /**
     * Xây dựng DTO thông báo từ Job entity
     */
    private JobNotificationDTO buildJobNotification(Job job) {
        List<String> skillNames = job.getSkills() != null ? job.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toList()) : List.of();

        // Company company =
        // this.companyService.findById(job.getCompany().getId()).isPresent() ?
        // this.companyService.findById(job.getCompany().getId()).get() : null;

        String companyName = job.getCompany() != null ? job.getCompany().getName() : "Unknown Company";
        String companyLogo = job.getCompany() != null ? job.getCompany().getLogo() : null;

        return JobNotificationDTO.builder()
                .jobId(job.getId())
                .jobName(job.getName())
                .companyName(companyName)
                .companyLogo(companyLogo)
                .location(job.getLocation())
                .salary(job.getSalary())
                .level(job.getLevel())
                .jobType(job.getJobType())
                .skills(skillNames)
                .createdAt(job.getCreatedAt())
                .message("Có công việc mới phù hợp với kỹ năng của bạn!")
                .build();
    }

    /**
     * Lấy danh sách notifications của user (có phân trang)
     */
    public Page<JobNotification> getUserNotifications(Long userId, Pageable pageable) {
        return jobNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Đếm số notifications chưa đọc
     */
    public long countUnreadNotifications(Long userId) {
        return jobNotificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * Đánh dấu một notification là đã đọc
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        JobNotification notification = jobNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        jobNotificationRepository.save(notification);
    }

    /**
     * Đánh dấu tất cả notifications của user là đã đọc
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        jobNotificationRepository.markAllAsReadByUserId(userId);
    }

    /**
     * Lấy danh sách notifications chưa đọc
     */
    public List<JobNotification> getUnreadNotifications(Long userId) {
        return jobNotificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Gửi custom notification (Welcome, Announcement, Promotion)
     * Không cần tạo entity mới, tái sử dụng JobNotification
     */
    @Transactional
    public Map<String, Object> sendCustomNotification(String title, String message, String type, List<Long> userIds) {
        try {
            List<User> targetUsers;

            // Nếu userIds null/empty -> gửi tất cả verified users
            if (userIds == null || userIds.isEmpty()) {
                targetUsers = userRepository.findAll().stream()
                        .filter(User::isVerified)
                        .collect(Collectors.toList());
                log.info("Sending custom notification to ALL verified users ({})", targetUsers.size());
            } else {
                targetUsers = userRepository.findAllById(userIds);
                log.info("Sending custom notification to {} specific users", targetUsers.size());
            }

            if (targetUsers.isEmpty()) {
                return buildCustomResponse(0, 0, "No users found");
            }

            // DTO cho WebSocket
            Map<String, Object> notificationDTO = new HashMap<>();
            notificationDTO.put("title", title);
            notificationDTO.put("message", message);
            notificationDTO.put("type", type != null ? type : "ANNOUNCEMENT");
            notificationDTO.put("createdAt", java.time.Instant.now().toString());

            List<String> userIdStrings = targetUsers.stream()
                    .map(u -> String.valueOf(u.getId()))
                    .collect(Collectors.toList());

            List<WebSocketSession> sessions = webSocketSessionService.getSessionsByUserIds(userIdStrings);

            int saved = 0, sent = 0;

            for (User user : targetUsers) {
                // Lưu vào DB (job_id = null cho custom notifications)
                JobNotification notification = JobNotification.builder()
                        .user(user)
                        .job(null) // Không liên quan đến job cụ thể
                        .title(title)
                        .message(message)
                        .type(type != null ? type : "ANNOUNCEMENT")
                        .read(false)
                        .build();
                jobNotificationRepository.save(notification);
                saved++;

                // Gửi WebSocket
                String userId = String.valueOf(user.getId());
                for (WebSocketSession session : sessions) {
                    if (session.getUserId().equals(userId)) {
                        try {
                            socketIOServer.getClient(java.util.UUID.fromString(session.getSocketSessionId()))
                                    .sendEvent("customNotification", notificationDTO);
                            sent++;
                        } catch (Exception e) {
                            log.error("Failed to send to session {}", session.getSocketSessionId());
                        }
                    }
                }
            }

            log.info("Custom notification: {} saved, {} sent via WebSocket", saved, sent);
            return buildCustomResponse(saved, sent, "Success");

        } catch (Exception e) {
            log.error("Error sending custom notification: {}", e.getMessage(), e);
            return buildCustomResponse(0, 0, "Error: " + e.getMessage());
        }
    }

    private Map<String, Object> buildCustomResponse(int saved, int sent, String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("savedToDatabase", saved);
        result.put("sentViaWebSocket", sent);
        result.put("message", msg);
        return result;
    }
}

package vn.hoidanit.jobhunter.service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceService {
    private final UserService userService;

    // userId -> connection count (hỗ trợ nhiều tab/thiết bị)
    private final Map<Long, Integer> onlineCounts = new ConcurrentHashMap<>();

    /**
     * Đánh dấu user đã kết nối
     */
    public void userConnected(Long userId) {
        onlineCounts.merge(userId, 1, Integer::sum);
        log.info("User {} connected. Active connections: {}", userId, onlineCounts.get(userId));
    }

    /**
     * Đánh dấu user đã ngắt kết nối
     * @return true nếu user hoàn toàn offline (không còn connection nào)
     */
    public boolean userDisconnected(Long userId) {
        Integer count = onlineCounts.computeIfPresent(userId, (k, v) -> v <= 1 ? null : v - 1);
        boolean completelyOffline = count == null;

        if (completelyOffline) {
            // Cập nhật lastSeen vào database
            updateLastSeen(userId);
            log.info("User {} completely offline. LastSeen updated.", userId);
        } else {
            log.info("User {} disconnected. Remaining connections: {}", userId, count);
        }

        return completelyOffline;
    }

    /**
     * Kiểm tra user có online không
     */
    public boolean isOnline(Long userId) {
        return onlineCounts.containsKey(userId);
    }

    /**
     * Lấy tất cả user đang online
     */
    public Set<Long> getAllOnlineUserIds() {
        return onlineCounts.keySet();
    }

    /**
     * Lấy số lượng connections của một user
     */
    public int getConnectionCount(Long userId) {
        return onlineCounts.getOrDefault(userId, 0);
    }

    /**
     * Cập nhật lastSeen vào database
     */
    private void updateLastSeen(Long userId) {
        try {
            userService.updateLastSeen(userId, Instant.now());
        } catch (Exception e) {
            log.error("Error updating lastSeen for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Lấy thời gian hiện tại
     */
    public Instant getCurrentTime() {
        return Instant.now();
    }
}


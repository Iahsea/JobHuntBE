package vn.hoidanit.jobhunter.domain.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomNotificationRequest {

    /**
     * Tiêu đề notification (bắt buộc)
     */
    String title;

    /**
     * Nội dung chi tiết (bắt buộc)
     */
    String message;

    /**
     * Loại notification: WELCOME, ANNOUNCEMENT, PROMOTION, UPDATE, REMINDER
     * Default: ANNOUNCEMENT nếu null
     */
    String type;

    /**
     * Danh sách user IDs cần gửi
     * Nếu null hoặc empty -> gửi đến TẤT CẢ users verified
     */
    List<Long> userIds;
}


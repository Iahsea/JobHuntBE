package vn.hoidanit.jobhunter.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingEventRequest {
    private Long conversationId;
    private Long userId;
    private String userName;
    private boolean isTyping;
}


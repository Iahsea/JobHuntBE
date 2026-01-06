package vn.hoidanit.jobhunter.domain.response.chat;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.jobhunter.domain.ChatMessage;

@Getter
@Setter
public class ResChatMessageDTO {
    private long id;
    private String role;
    private String content;
    private Instant createdAt;

    // For file messages
    private ChatMessage.MessageType messageType;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
}

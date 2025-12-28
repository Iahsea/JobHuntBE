package vn.hoidanit.jobhunter.domain.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.hoidanit.jobhunter.domain.ChatRealtimeMessage;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRealtimeMessageRequest {
    @NotBlank
    String conversationId;

    String message;

    @Builder.Default
    ChatRealtimeMessage.MessageType messageType = ChatRealtimeMessage.MessageType.TEXT;

    // For IMAGE and FILE messages
    String fileUrl;
    String fileName;
    Long fileSize;
}

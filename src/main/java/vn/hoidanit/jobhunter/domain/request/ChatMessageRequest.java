package vn.hoidanit.jobhunter.domain.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.hoidanit.jobhunter.domain.ChatMessage;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageRequest {
    @NotBlank
    String conversationId;

    String message;

    @Builder.Default
    ChatMessage.MessageType messageType = ChatMessage.MessageType.TEXT;

    // For IMAGE and FILE messages
    String fileUrl;
    String fileName;
    Long fileSize;
}

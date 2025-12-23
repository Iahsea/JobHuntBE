package vn.hoidanit.jobhunter.domain.response;

import java.time.Instant;


import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.hoidanit.jobhunter.domain.ChatMessage;
import vn.hoidanit.jobhunter.domain.ParticipantInfo;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {
    String id;
    Long conversationId;
    boolean me;
    String message;
    ParticipantInfo sender;
    Instant createdDate;

    ChatMessage.MessageType messageType;
    String fileUrl;
    String fileName;
    Long fileSize;
}

package vn.hoidanit.jobhunter.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_message")
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRealtimeMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String conversationId;

    String message;

    @Embedded
    ParticipantInfo sender;

    Instant createdDate;

    // Message type: TEXT, IMAGE, FILE
    @Enumerated(EnumType.STRING)
    @Builder.Default
    MessageType messageType = MessageType.TEXT;

    // For IMAGE and FILE messages
    String fileUrl;
    String fileName;
    Long fileSize; // in bytes

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE
    }

}

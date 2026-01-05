package vn.hoidanit.jobhunter.domain;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import vn.hoidanit.jobhunter.util.SecurityUtil;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private ChatSession chatSession;

    @NotBlank(message = "Role không được để trống")
    private String role; // "user" hoặc "assistant"

    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    // Message type: TEXT, FILE
    @Enumerated(EnumType.STRING)
    private MessageType messageType = MessageType.TEXT;

    // For FILE messages
    private String fileUrl;
    private String fileName;
    private Long fileSize; // in bytes

    private Instant createdAt;
    private String createdBy;

    public enum MessageType {
        TEXT,
        FILE
    }

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        this.createdAt = Instant.now();
        if (this.messageType == null) {
            this.messageType = MessageType.TEXT;
        }
    }
}

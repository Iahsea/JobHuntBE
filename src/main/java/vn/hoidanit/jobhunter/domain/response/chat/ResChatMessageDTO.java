package vn.hoidanit.jobhunter.domain.response.chat;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResChatMessageDTO {
    private long id;
    private String role;
    private String content;
    private Instant createdAt;
}

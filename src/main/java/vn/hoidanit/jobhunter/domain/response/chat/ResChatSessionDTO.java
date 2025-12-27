package vn.hoidanit.jobhunter.domain.response.chat;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResChatSessionDTO {
    private long id;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ResChatMessageDTO> messages;
}

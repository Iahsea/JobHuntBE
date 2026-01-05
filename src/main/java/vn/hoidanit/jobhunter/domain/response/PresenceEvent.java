package vn.hoidanit.jobhunter.domain.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceEvent {
    private Long userId;
    private boolean online;
    private Instant lastSeen;
}


package vn.hoidanit.jobhunter.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "websocket_sessions")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebSocketSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String userId;

    String socketSessionId;

    Instant createdAt;
}

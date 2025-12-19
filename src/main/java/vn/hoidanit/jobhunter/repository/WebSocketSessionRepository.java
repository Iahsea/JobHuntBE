package vn.hoidanit.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.hoidanit.jobhunter.domain.WebSocketSession;

import java.util.Arrays;
import java.util.List;

public interface WebSocketSessionRepository extends JpaRepository<WebSocketSession, Long> {
    List<WebSocketSession> findAllByUserIdIn(List<String> participantIds);

    void deleteBySocketSessionId(String socketSessionId);
}

package vn.hoidanit.jobhunter.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.domain.WebSocketSession;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.repository.WebSocketSessionRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketSessionService {
    WebSocketSessionRepository webSocketSessionRepository;

    @Transactional
    public void create(WebSocketSession webSocketSession) {
        webSocketSessionRepository.save(webSocketSession);
    }

    @Transactional
    public void deleteBySocketSessionId(String socketSessionId) {
        webSocketSessionRepository.deleteBySocketSessionId(socketSessionId);
    }

    public List<WebSocketSession> getSessionsByUserIds(List<String> userIds) {
        return webSocketSessionRepository.findAllByUserIdIn(userIds);
    }

    public WebSocketSession getBySocketSessionId(String socketSessionId) {
        return webSocketSessionRepository.findBySocketSessionId(socketSessionId);
    }
}
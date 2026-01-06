package vn.hoidanit.jobhunter.controller;

import java.text.ParseException;
import java.time.Instant;

import com.nimbusds.jose.JOSEException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.domain.Conversation;
import vn.hoidanit.jobhunter.domain.WebSocketSession;
import vn.hoidanit.jobhunter.domain.request.TypingEventRequest;
import vn.hoidanit.jobhunter.domain.response.IntrospectResponse;
import vn.hoidanit.jobhunter.domain.response.PresenceEvent;
import vn.hoidanit.jobhunter.service.AuthService;
import vn.hoidanit.jobhunter.service.ConversationService;
import vn.hoidanit.jobhunter.service.PresenceService;
import vn.hoidanit.jobhunter.service.WebSocketSessionService;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketHandler {
    SocketIOServer server;
    WebSocketSessionService webSocketSessionService;
    AuthService authService;
    ConversationService conversationService;
    PresenceService presenceService;

    @OnConnect
    public void clientConnected(SocketIOClient client) throws IdInvalidException, ParseException, JOSEException {
        // Get Token from request param
        String token = client.getHandshakeData().getSingleUrlParam("token");

        log.info("TOKEN: {}", token);

        // Verify token
        IntrospectResponse introspect = authService.introspect(token, false);
        // If Token is invalid disconnect
        if (introspect.isValid()) {
            log.info("Client connected: {}", client.getSessionId());
            String userIdString = introspect.getUserId();
            Long userId = Long.parseLong(userIdString);

            WebSocketSession webSocketSession = WebSocketSession.builder()
                    .userId(userIdString)
                    .socketSessionId(client.getSessionId().toString())
                    .createdAt(Instant.now())
                    .build();

            webSocketSessionService.create(webSocketSession);
            log.info("WebSocket session created for user: {}", userId);

            // Bước 1: Gửi danh sách tất cả users đang online CHO USER MỚI này trước
            java.util.Set<Long> currentOnlineUsers = presenceService.getAllOnlineUserIds();
            for (Long onlineUserId : currentOnlineUsers) {
                PresenceEvent existingUserEvent = PresenceEvent.builder()
                        .userId(onlineUserId)
                        .online(true)
                        .lastSeen(null)
                        .build();
                client.sendEvent("userPresence", existingUserEvent);
            }
            log.info("Sent {} existing online users to new client: {}", currentOnlineUsers.size(), userId);

            // Bước 2: Đánh dấu user MỚI này là online
            presenceService.userConnected(userId);

            // Bước 3: Broadcast presence event của USER MỚI này đến TẤT CẢ clients (bao gồm cả chính nó)
            PresenceEvent newUserPresenceEvent = PresenceEvent.builder()
                    .userId(userId)
                    .online(true)
                    .lastSeen(null)
                    .build();
            server.getBroadcastOperations().sendEvent("userPresence", newUserPresenceEvent);
            log.info("Broadcasted online status for user: {}", userId);
        } else {
            log.error("Authentication fail: {}", client.getSessionId());
            client.disconnect();
        }
    }

    @OnDisconnect
    public void clientDisconnected(SocketIOClient client) {
        log.info("Client disConnected: {}", client.getSessionId());

        // Lấy thông tin user từ session trước khi xóa
        WebSocketSession session = webSocketSessionService.getBySocketSessionId(client.getSessionId().toString());

        if (session != null) {
            Long userId = Long.parseLong(session.getUserId());

            // Xóa session
            webSocketSessionService.deleteBySocketSessionId(client.getSessionId().toString());

            // Đánh dấu user disconnected và kiểm tra xem có hoàn toàn offline không
            boolean completelyOffline = presenceService.userDisconnected(userId);

            // Broadcast presence event
            PresenceEvent presenceEvent = PresenceEvent.builder()
                    .userId(userId)
                    .online(!completelyOffline)
                    .lastSeen(completelyOffline ? presenceService.getCurrentTime() : null)
                    .build();
            server.getBroadcastOperations().sendEvent("userPresence", presenceEvent);
            log.info("Broadcasted {} status for user: {}", completelyOffline ? "offline" : "still online", userId);
        } else {
            webSocketSessionService.deleteBySocketSessionId(client.getSessionId().toString());
        }
    }

    @OnEvent("typing")
    public void handleTypingEvent(SocketIOClient client, TypingEventRequest typingEvent) {
        log.info("Typing event received: conversationId={}, userId={}, isTyping={}",
                typingEvent.getConversationId(),
                typingEvent.getUserId(),
                typingEvent.isTyping());

        try {
            // Get conversation to find other participants
            Conversation conversation = conversationService.getConversationById(typingEvent.getConversationId());

            if (conversation == null) {
                log.error("Conversation not found: {}", typingEvent.getConversationId());
                return;
            }

            // Get all participant IDs except the sender
            List<String> otherParticipantIds = conversation.getParticipants().stream()
                    .map(p -> p.getUserId())
                    .filter(userId -> !userId.equals(String.valueOf(typingEvent.getUserId())))
                    .collect(Collectors.toList());

            log.info("Broadcasting typing event to participants: {}", otherParticipantIds);

            // Get active WebSocket sessions for other participants
            List<WebSocketSession> activeSessions = webSocketSessionService.getSessionsByUserIds(otherParticipantIds);

            // Broadcast typing event to all active sessions
            for (WebSocketSession session : activeSessions) {
                UUID sessionId = UUID.fromString(session.getSocketSessionId());
                SocketIOClient targetClient = server.getClient(sessionId);

                if (targetClient != null) {
                    targetClient.sendEvent("userTyping", typingEvent);
                    log.info("Sent typing event to session: {}", session.getSocketSessionId());
                }
            }

        } catch (Exception e) {
            log.error("Error handling typing event: {}", e.getMessage(), e);
        }
    }

    @PostConstruct
    public void startServer() {
        server.start();
        server.addListeners(this);
        log.info("Socket server started");
    }

    @PreDestroy
    public void stopServer() {
        server.stop();
        log.info("Socket server stoped");
    }
}

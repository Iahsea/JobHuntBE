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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.domain.WebSocketSession;
import vn.hoidanit.jobhunter.domain.response.IntrospectResponse;
import vn.hoidanit.jobhunter.service.AuthService;
import vn.hoidanit.jobhunter.service.WebSocketSessionService;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocketHandler {
    SocketIOServer server;
    WebSocketSessionService webSocketSessionService;
    AuthService authService;

    @OnConnect
    public void clientConnected(SocketIOClient client) throws IdInvalidException, ParseException, JOSEException {
        // Get Token from request param
        String token = client.getHandshakeData().getSingleUrlParam("token");

        // Verify token
        IntrospectResponse introspect = authService.introspect(token, false);
        // If Token is invalid disconnect
        if (introspect.isValid()) {
            log.info("Client connected: {}", client.getSessionId());
            WebSocketSession webSocketSession = WebSocketSession.builder()
                    .userId(introspect.getUserId())
                    .socketSessionId(client.getSessionId().toString())
                    .createdAt(Instant.now())
                    .build();

            webSocketSessionService.create(webSocketSession);
            log.info("WebSocket session created for user: {}", introspect.getUserId());
        } else {
            log.error("Authentication fail: {}", client.getSessionId());
            client.disconnect();
        }
    }

    @OnDisconnect
    public void clientDisconnected(SocketIOClient client) {
        log.info("Client disConnected: {}", client.getSessionId());
        webSocketSessionService.deleteBySocketSessionId(client.getSessionId().toString());
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

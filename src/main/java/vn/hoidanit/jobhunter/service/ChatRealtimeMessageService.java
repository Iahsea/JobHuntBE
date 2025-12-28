package vn.hoidanit.jobhunter.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.domain.ChatRealtimeMessage;
import vn.hoidanit.jobhunter.domain.ParticipantInfo;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.WebSocketSession;
import vn.hoidanit.jobhunter.domain.request.ChatRealtimeMessageRequest;
import vn.hoidanit.jobhunter.domain.response.ChatRealtimeMessageResponse;
import vn.hoidanit.jobhunter.mapper.ChatRealtimeMessageMapper;
import vn.hoidanit.jobhunter.repository.ChatRealtimeMessageRepository;
import vn.hoidanit.jobhunter.repository.ConversationRepository;
import vn.hoidanit.jobhunter.repository.WebSocketSessionRepository;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatRealtimeMessageService {
    SocketIOServer socketIOServer;

    ChatRealtimeMessageRepository chatRealtimeMessageRepository;
    ConversationRepository conversationRepository;
    UserService userService;

    ChatRealtimeMessageMapper chatRealtimeMessageMapper;

    WebSocketSessionRepository webSocketSessionRepository;
    ObjectMapper objectMapper;

    public List<ChatRealtimeMessageResponse> getMessages(String conversationId) throws IdInvalidException {
        // Validate conversationId
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User userInfo = userService.handleGetUserByUsername(email);
        String userId = String.valueOf(userInfo.getId());
        conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new IdInvalidException("Conversation ID is invalid"))
                .getParticipants()
                .stream()
                .filter(participantInfo -> userId.equals(participantInfo.getUserId()))
                .findAny()
                .orElseThrow(() -> new IdInvalidException("Conversation ID is invalid 123"));

        var messages = chatRealtimeMessageRepository.findAllByConversationIdOrderByCreatedDateDesc(conversationId);

        return messages.stream().map(chatRealtimeMessage -> this.toChatRealtimeMessageResponse(chatRealtimeMessage, userId)).toList();
    }

    public ChatRealtimeMessageResponse create(ChatRealtimeMessageRequest request) throws JsonProcessingException, IdInvalidException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // Validate conversationId
        User userInfo = userService.handleGetUserByUsername(email);
        String userId = String.valueOf(userInfo.getId());
        log.info("User ID: {}", userId);

        // Validate based on message type
        if (request.getMessageType() == ChatRealtimeMessage.MessageType.TEXT) {
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                throw new IdInvalidException("Message content is required for TEXT messages");
            }
        } else if (request.getMessageType() == ChatRealtimeMessage.MessageType.IMAGE ||
                   request.getMessageType() == ChatRealtimeMessage.MessageType.FILE) {
            if (request.getFileUrl() == null || request.getFileUrl().trim().isEmpty()) {
                throw new IdInvalidException("File URL is required for IMAGE/FILE messages");
            }
        }

        var coversation = conversationRepository
                .findById(request.getConversationId())
                .orElseThrow(() -> new IdInvalidException("Conversation ID is invalid"));

        coversation.getParticipants().stream()
                .filter(participantInfo -> userId.equals(participantInfo.getUserId()))
                .findAny()
                .orElseThrow(() -> new IdInvalidException("Conversation ID is invalid 123"));

        // Get UserInfo from ProfileService
//        var userResponse = profileClient.getProfile(userId);
//        if (Objects.isNull(userResponse)) {
//            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
//        }
        // Build Chat message Info
        ChatRealtimeMessage chatRealtimeMessage = chatRealtimeMessageMapper.toChatRealtimeMessage(request);
        chatRealtimeMessage.setSender(ParticipantInfo.builder()
                .userId(userId)
                .username(userInfo.getCompany() != null ? userInfo.getCompany().getName() : userInfo.getName())
                .name(userInfo.getCompany() != null ? userInfo.getCompany().getName() : userInfo.getName())
                .avatar(userInfo.getCompany() != null ? userInfo.getCompany().getLogo() : userInfo.getAvatar())
                .build());
        chatRealtimeMessage.setCreatedDate(Instant.now());

        // Create chat message
        chatRealtimeMessage = chatRealtimeMessageRepository.save(chatRealtimeMessage);

        // get participants to send message
        List<String> participantIds = coversation.getParticipants().stream()
                .map(ParticipantInfo::getUserId)
                .toList();

        Map<String, WebSocketSession> webSocketSessionList =
                webSocketSessionRepository.findAllByUserIdIn(participantIds).stream()
                        .collect(Collectors.toMap(WebSocketSession::getSocketSessionId, Function.identity()));

        // Publish socket event to clients
        ChatRealtimeMessageResponse chatRealtimeMessageResponse = chatRealtimeMessageMapper.toChatRealtimeMessageResponse(chatRealtimeMessage);
        socketIOServer.getAllClients().forEach(client -> {
            var webSocketSession =
                    webSocketSessionList.get(client.getSessionId().toString());

            if (Objects.nonNull(webSocketSession)) {
                try {
                    chatRealtimeMessageResponse.setMe(webSocketSession.getUserId().equals(userId));
                    client.sendEvent("message", objectMapper.writeValueAsString(chatRealtimeMessageResponse));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // convert to Response
        return toChatRealtimeMessageResponse(chatRealtimeMessage, userId);
    }

    private ChatRealtimeMessageResponse toChatRealtimeMessageResponse(ChatRealtimeMessage chatRealtimeMessage, String userId) {
        var chatRealtimeMessageResponse = chatRealtimeMessageMapper.toChatRealtimeMessageResponse(chatRealtimeMessage);

        chatRealtimeMessageResponse.setMe(userId.equals(chatRealtimeMessage.getSender().getUserId()));

        return chatRealtimeMessageResponse;
    }
}

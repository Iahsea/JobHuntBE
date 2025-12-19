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
import vn.hoidanit.jobhunter.domain.ChatMessage;
import vn.hoidanit.jobhunter.domain.ParticipantInfo;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.WebSocketSession;
import vn.hoidanit.jobhunter.domain.request.ChatMessageRequest;
import vn.hoidanit.jobhunter.domain.response.ChatMessageResponse;
import vn.hoidanit.jobhunter.mapper.ChatMessageMapper;
import vn.hoidanit.jobhunter.repository.ChatMessageRepository;
import vn.hoidanit.jobhunter.repository.ConversationRepository;
import vn.hoidanit.jobhunter.repository.WebSocketSessionRepository;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageService {
    SocketIOServer socketIOServer;

    ChatMessageRepository chatMessageRepository;
    ConversationRepository conversationRepository;
    UserService userService;

    ChatMessageMapper chatMessageMapper;

    WebSocketSessionRepository webSocketSessionRepository;
    ObjectMapper objectMapper;

    public List<ChatMessageResponse> getMessages(String conversationId) throws IdInvalidException {
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

        var messages = chatMessageRepository.findAllByConversationIdOrderByCreatedDateDesc(conversationId);

        return messages.stream().map(chatMessage -> this.toChatMessageResponse(chatMessage, userId)).toList();
    }

    public ChatMessageResponse create(ChatMessageRequest request) throws JsonProcessingException, IdInvalidException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // Validate conversationId
        User userInfo = userService.handleGetUserByUsername(email);
        String userId = String.valueOf(userInfo.getId());
        log.info("User ID: {}", userId);
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
        ChatMessage chatMessage = chatMessageMapper.toChatMessage(request);
        chatMessage.setSender(ParticipantInfo.builder()
                .userId(userId)
                .username(userInfo.getEmail())
                .name(userInfo.getName())
                .avatar(userInfo.getAvatar())
                .build());
        chatMessage.setCreatedDate(Instant.now());

        // Create chat message
        chatMessage = chatMessageRepository.save(chatMessage);
        String message = objectMapper.writeValueAsString(chatMessage);

        // get participants to send message
        List<String> participantIds = coversation.getParticipants().stream()
                .map(ParticipantInfo::getUserId)
                .toList();

        Map<String, WebSocketSession> webSocketSessionList =
                webSocketSessionRepository.findAllByUserIdIn(participantIds).stream()
                        .collect(Collectors.toMap(WebSocketSession::getSocketSessionId, Function.identity()));

        // Publish socket event to clients
        ChatMessageResponse chatMessageResponse = chatMessageMapper.toChatMessageResponse(chatMessage);
        socketIOServer.getAllClients().forEach(client -> {
            var webSocketSession =
                    webSocketSessionList.get(client.getSessionId().toString());

            if (Objects.nonNull(webSocketSession)) {
                try {
                    chatMessageResponse.setMe(webSocketSession.getUserId().equals(userId));
                    client.sendEvent("message", objectMapper.writeValueAsString(chatMessageResponse));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // convert to Response
        return toChatMessageResponse(chatMessage, userId);
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage, String userId) {
        var chatMessageResponse = chatMessageMapper.toChatMessageResponse(chatMessage);

        chatMessageResponse.setMe(userId.equals(chatMessage.getSender().getUserId()));

        return chatMessageResponse;
    }
}

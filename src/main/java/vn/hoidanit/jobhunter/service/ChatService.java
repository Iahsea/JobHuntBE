package vn.hoidanit.jobhunter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import vn.hoidanit.jobhunter.domain.ChatMessage;
import vn.hoidanit.jobhunter.domain.ChatSession;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.ChatbotRequest;
import vn.hoidanit.jobhunter.domain.response.ChatbotResponse;
import vn.hoidanit.jobhunter.domain.response.chat.ResChatMessageDTO;
import vn.hoidanit.jobhunter.domain.response.chat.ResChatSessionDTO;
import vn.hoidanit.jobhunter.repository.ChatMessageRepository;
import vn.hoidanit.jobhunter.repository.ChatSessionRepository;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@Service
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final RestTemplate restTemplate;

    @Value("${chatbot.python.api.url:http://localhost:8000}")
    private String pythonApiUrl;

    public ChatService(
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository,
            UserService userService,
            RestTemplate restTemplate) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    /**
     * Tạo session chat mới cho user
     */
    public ResChatSessionDTO createSession(String title, String email) throws IdInvalidException {
        User user = this.userService.handleGetUserByUsername(email);
        if (user == null) {
            throw new IdInvalidException("User không tồn tại");
        }

        ChatSession session = new ChatSession();
        session.setTitle(title);
        session.setUser(user);

        ChatSession chatSession = chatSessionRepository.save(session);
        return convertToDTO(chatSession);
    }

    /**
     * Lưu một message vào database
     */
    public ChatMessage addMessage(long sessionId, String role, String content) throws IdInvalidException {
        Optional<ChatSession> sessionOptional = this.chatSessionRepository.findById(sessionId);
        if (!sessionOptional.isPresent()) {
            throw new IdInvalidException("Chat session không tồn tại");
        }

        ChatMessage message = new ChatMessage();
        message.setChatSession(sessionOptional.get());
        message.setRole(role);
        message.setContent(content);

        ChatMessage savedMessage = this.chatMessageRepository.save(message);

        // Cập nhật updatedAt của session
        ChatSession session = sessionOptional.get();
        session.setUpdatedAt(java.time.Instant.now());
        this.chatSessionRepository.save(session);

        return savedMessage;
    }

    /**
     * Gửi tin nhắn tới Python AI và nhận phản hồi
     * Lưu cả tin nhắn user và phản hồi AI vào database
     */
    public ChatbotResponse sendMessageToAI(long sessionId, String userMessage) throws IdInvalidException {
        Optional<ChatSession> sessionOptional = this.chatSessionRepository.findById(sessionId);
        if (!sessionOptional.isPresent()) {
            throw new IdInvalidException("Chat session không tồn tại");
        }

        ChatSession session = sessionOptional.get();

        // Lấy lịch sử hội thoại từ database (giới hạn 20 tin nhắn gần nhất để tiết kiệm
        // token)
        List<ChatMessage> history = this.chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session);
        int startIndex = Math.max(0, history.size() - 20);
        history = history.subList(startIndex, history.size());

        // Chuẩn bị request cho Python API
        ChatbotRequest request = new ChatbotRequest();
        request.setMessage(userMessage);

        // Convert lịch sử sang format Python API yêu cầu
        List<ChatbotRequest.ConversationHistory> conversationHistory = new ArrayList<>();
        for (ChatMessage msg : history) {
            ChatbotRequest.ConversationHistory historyItem = new ChatbotRequest.ConversationHistory();
            historyItem.setRole(msg.getRole());
            historyItem.setContent(msg.getContent());
            conversationHistory.add(historyItem);
        }
        request.setConversationHistory(conversationHistory);

        // Gọi Python API
        try {
            ChatbotResponse response = restTemplate.postForObject(
                    pythonApiUrl + "/api/chat",
                    request,
                    ChatbotResponse.class);

            // Lưu tin nhắn user vào database
            addMessage(sessionId, "user", userMessage);

            // Lưu phản hồi AI vào database
            if (response != null && response.isSuccess()) {
                addMessage(sessionId, "assistant", response.getResponse());
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi Python AI API: " + e.getMessage());
        }
    }

    /**
     * Lấy tất cả sessions của user
     */
    public List<ResChatSessionDTO> getUserSessions(String email) throws IdInvalidException {
        User user = this.userService.handleGetUserByUsername(email);
        if (user == null) {
            throw new IdInvalidException("User không tồn tại");
        }

        List<ChatSession> sessions = this.chatSessionRepository.findByUserOrderByUpdatedAtDesc(user);
        return sessions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một session kèm tất cả messages
     */
    public ResChatSessionDTO getSessionWithMessages(long sessionId) throws IdInvalidException {
        Optional<ChatSession> sessionOptional = this.chatSessionRepository.findById(sessionId);
        if (!sessionOptional.isPresent()) {
            throw new IdInvalidException("Chat session không tồn tại");
        }

        ChatSession session = sessionOptional.get();
        List<ChatMessage> messages = this.chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session);

        ResChatSessionDTO dto = convertToDTO(session);
        dto.setMessages(messages.stream().map(this::convertMessageToDTO).collect(Collectors.toList()));
        return dto;
    }

    /**
     * Xóa một session
     */
    public void deleteSession(long sessionId) throws IdInvalidException {
        Optional<ChatSession> sessionOptional = this.chatSessionRepository.findById(sessionId);
        if (!sessionOptional.isPresent()) {
            throw new IdInvalidException("Chat session không tồn tại");
        }
        this.chatSessionRepository.deleteById(sessionId);
    }

    /**
     * Cập nhật title của session
     */
    public ChatSession updateSessionTitle(long sessionId, String newTitle) throws IdInvalidException {
        Optional<ChatSession> sessionOptional = this.chatSessionRepository.findById(sessionId);
        if (!sessionOptional.isPresent()) {
            throw new IdInvalidException("Chat session không tồn tại");
        }

        ChatSession session = sessionOptional.get();
        session.setTitle(newTitle);
        return this.chatSessionRepository.save(session);
    }

    // Helper methods
    private ResChatSessionDTO convertToDTO(ChatSession session) {
        ResChatSessionDTO dto = new ResChatSessionDTO();
        dto.setId(session.getId());
        dto.setTitle(session.getTitle());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        return dto;
    }

    private ResChatMessageDTO convertMessageToDTO(ChatMessage message) {
        ResChatMessageDTO dto = new ResChatMessageDTO();
        dto.setId(message.getId());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}

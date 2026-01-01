package vn.hoidanit.jobhunter.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import vn.hoidanit.jobhunter.domain.ChatSession;
import vn.hoidanit.jobhunter.domain.response.ChatbotResponse;
import vn.hoidanit.jobhunter.domain.response.chat.ResChatSessionDTO;
import vn.hoidanit.jobhunter.service.ChatService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/sessions")
    @ApiMessage("Create a new chat session")
    public ResponseEntity<ResChatSessionDTO> createSession(@RequestBody CreateSessionRequest request)
            throws IdInvalidException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        ResChatSessionDTO sessionDTO = this.chatService.createSession(request.getTitle(), email);
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionDTO);
    }

    @PostMapping("/sessions/{id}/chat")
    @ApiMessage("Send message to AI and get response")
    public ResponseEntity<ChatbotResponse> chatWithAI(
            @PathVariable("id") long sessionId,
            @RequestBody SendMessageRequest request) throws IdInvalidException {

        ChatbotResponse response = this.chatService.sendMessageToAI(sessionId, request.getMessage());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions")
    @ApiMessage("Get all user chat sessions")
    public ResponseEntity<List<ResChatSessionDTO>> getUserSessions() throws IdInvalidException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        List<ResChatSessionDTO> sessions = this.chatService.getUserSessions(email);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/{id}")
    @ApiMessage("Get chat session with messages")
    public ResponseEntity<ResChatSessionDTO> getSession(@PathVariable("id") long sessionId)
            throws IdInvalidException {
        ResChatSessionDTO session = this.chatService.getSessionWithMessages(sessionId);
        return ResponseEntity.ok(session);
    }

    @PutMapping("/sessions/{id}")
    @ApiMessage("Update chat session title")
    public ResponseEntity<ChatSession> updateSessionTitle(
            @PathVariable("id") long sessionId,
            @RequestBody UpdateSessionRequest request) throws IdInvalidException {
        ChatSession session = this.chatService.updateSessionTitle(sessionId, request.getTitle());
        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/sessions/{id}")
    @ApiMessage("Delete chat session")
    public ResponseEntity<Void> deleteSession(@PathVariable("id") long sessionId)
            throws IdInvalidException {
        this.chatService.deleteSession(sessionId);
        return ResponseEntity.ok().build();
    }

    // Inner classes for request bodies
    @lombok.Getter
    @lombok.Setter
    public static class CreateSessionRequest {
        private String title;
    }

    @lombok.Getter
    @lombok.Setter
    public static class SendMessageRequest {
        private String message;
    }

    @lombok.Getter
    @lombok.Setter
    public static class UpdateSessionRequest {
        private String title;
    }
}

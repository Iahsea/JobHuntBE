package vn.hoidanit.jobhunter.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ChatbotRequest {
    private String message;

    @JsonProperty("conversation_history")
    private List<ConversationHistory> conversationHistory;

    @Getter
    @Setter
    public static class ConversationHistory {
        private String role; // "user" or "assistant"
        private String content;
    }
}

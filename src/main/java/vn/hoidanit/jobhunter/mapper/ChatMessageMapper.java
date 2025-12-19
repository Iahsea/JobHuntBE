package vn.hoidanit.jobhunter.mapper;

import org.mapstruct.Mapper;
import vn.hoidanit.jobhunter.domain.ChatMessage;
import vn.hoidanit.jobhunter.domain.request.ChatMessageRequest;
import vn.hoidanit.jobhunter.domain.response.ChatMessageResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);

    ChatMessage toChatMessage(ChatMessageRequest request);

    List<ChatMessageResponse> toChatMessageResponses(List<ChatMessage> chatMessages);
}

package vn.hoidanit.jobhunter.mapper;

import org.mapstruct.Mapper;
import vn.hoidanit.jobhunter.domain.ChatRealtimeMessage;
import vn.hoidanit.jobhunter.domain.request.ChatRealtimeMessageRequest;
import vn.hoidanit.jobhunter.domain.response.ChatRealtimeMessageResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatRealtimeMessageMapper {
    ChatRealtimeMessageResponse toChatRealtimeMessageResponse(ChatRealtimeMessage chatRealtimeMessage);

    ChatRealtimeMessage toChatRealtimeMessage(ChatRealtimeMessageRequest request);

    List<ChatRealtimeMessageResponse> toChatRealtimeMessageResponses(List<ChatRealtimeMessage> chatRealtimeMessages);
}

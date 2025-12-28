package vn.hoidanit.jobhunter.mapper;

import org.mapstruct.Mapper;
import vn.hoidanit.jobhunter.domain.Conversation;
import vn.hoidanit.jobhunter.domain.request.ConversationRequest;
import vn.hoidanit.jobhunter.domain.response.ConversationResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    ConversationResponse toConversationResponse(Conversation conversation);

    List<ConversationResponse> toConversationResponses(List<Conversation> conversations);
}

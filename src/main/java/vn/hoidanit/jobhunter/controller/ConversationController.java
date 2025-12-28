package vn.hoidanit.jobhunter.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.request.ConversationRequest;
import vn.hoidanit.jobhunter.domain.response.ApiResponse;
import vn.hoidanit.jobhunter.domain.response.ConversationResponse;
import vn.hoidanit.jobhunter.service.ConversationService;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {

    ConversationService conversationService;

    @PostMapping("/conversations/create")
    ApiResponse<ConversationResponse> createConversation(@RequestBody @Valid ConversationRequest request) throws IdInvalidException {
        return ApiResponse.<ConversationResponse>builder()
                .result(conversationService.create(request))
                .build();
    }

    @GetMapping("/conversations/my-conversations")
    ApiResponse<List<ConversationResponse>> myConversations() {
        return ApiResponse.<List<ConversationResponse>>builder()
                .result(conversationService.myConversations())
                .build();
    }
}

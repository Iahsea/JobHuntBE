package vn.hoidanit.jobhunter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import vn.hoidanit.jobhunter.domain.request.ChatRealtimeMessageRequest;
import vn.hoidanit.jobhunter.domain.response.ApiResponse;
import vn.hoidanit.jobhunter.domain.response.ChatRealtimeMessageResponse;
import vn.hoidanit.jobhunter.service.ChatRealtimeMessageService;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/messages")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatRealtimeMessageController {
    ChatRealtimeMessageService chatRealtimeMessageService;

    @PostMapping("/create")
    ApiResponse<ChatRealtimeMessageResponse> create(@RequestBody @Valid ChatRealtimeMessageRequest request)
            throws JsonProcessingException, IdInvalidException {
        return ApiResponse.<ChatRealtimeMessageResponse>builder()
                .result(chatRealtimeMessageService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<ChatRealtimeMessageResponse>> getMessages(@RequestParam("conversationId") String conversationId) throws IdInvalidException {
        return ApiResponse.<List<ChatRealtimeMessageResponse>>builder()
                .result(chatRealtimeMessageService.getMessages(conversationId))
                .build();
    }
}

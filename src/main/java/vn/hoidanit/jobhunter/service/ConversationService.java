package vn.hoidanit.jobhunter.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.domain.Conversation;
import vn.hoidanit.jobhunter.domain.ParticipantInfo;
import vn.hoidanit.jobhunter.domain.request.ConversationRequest;
import vn.hoidanit.jobhunter.domain.response.ConversationResponse;
import vn.hoidanit.jobhunter.mapper.ConversationMapper;
import vn.hoidanit.jobhunter.repository.ConversationRepository;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationService {
    ConversationRepository conversationRepository;
    UserService userService;

    ConversationMapper conversationMapper;

    public List<ConversationResponse> myConversations() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userInfo = userService.handleGetUserByUsername(email);
        String userId = String.valueOf(userInfo.getId());
        List<Conversation> conversations = conversationRepository.findAllByParticipantIdsContains(userInfo.getId());

        return conversations.stream().map(conversation -> this.toConversationResponse(conversation, userId)).toList();
    }

    public ConversationResponse create(ConversationRequest request) throws IdInvalidException {
        // Fetch user infos
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var userInfo = userService.handleGetUserByUsername(email);
        String userId = String.valueOf(userInfo.getId());
        var participantInfo =
                userService.fetchUserById(Long.parseLong(request.getParticipantIds().getFirst()));

        if (Objects.isNull(participantInfo)) {
            throw new IdInvalidException("User or Participant ID is invalid");
        }

        List<Long> userIds = new ArrayList<>();
        userIds.add(userInfo.getId());
        userIds.add(participantInfo.getId());

        var sortedIds = userIds.stream().sorted().toList();
        String userIdHash = generateParticipantHash(sortedIds);

        var conversation = conversationRepository
                .findByParticipantsHash(userIdHash)
                .orElseGet(() -> {
                    List<ParticipantInfo> participantInfos = List.of(
                            ParticipantInfo.builder()
                                    .userId(userId)
                                    .username(userInfo.getEmail())
                                    .name(userInfo.getName())
                                    .avatar(userInfo.getAvatar())
                                    .build(),
                            ParticipantInfo.builder()
                                    .userId(request.getParticipantIds().getFirst())
                                    .username(participantInfo.getCompany().getName())
                                    .name(participantInfo.getCompany().getName())
                                    .avatar(participantInfo.getCompany().getLogo())
                                    .build());

                    // Build conversation info
                    Conversation newConversation = Conversation.builder()
                            .type(request.getType())
                            .participantsHash(userIdHash)
                            .createdDate(Instant.now())
                            .modifiedDate(Instant.now())
                            .participants(participantInfos)
                            .build();

                    return conversationRepository.save(newConversation);
                });

        return toConversationResponse(conversation, userId);
    }

    private String generateParticipantHash(List<Long> ids) {
        StringJoiner stringJoiner = new StringJoiner("_");
        for(Long id: ids) {;
            stringJoiner.add(String.valueOf(id));
        }

        // SHA 256

        return stringJoiner.toString();
    }

//    private ConversationResponse toConversationResponse(Conversation conversation, String userId) {
//        ConversationResponse conversationResponse = conversationMapper.toConversationResponse(conversation);
//
//        log.info("conversationResponse: {}", conversationResponse);
//
//        conversation.getParticipants().stream()
//                .filter(participantInfo -> !participantInfo.getUserId().equals(userId))
//                .findFirst()
//                .ifPresent(participantInfo -> {
//                    log.info("participantInfo123: {}", participantInfo);
//                    conversationResponse.setConversationName(participantInfo.getUsername());
//                    conversationResponse.setConversationAvatar(participantInfo.getAvatar());
//                });
//
//        return conversationResponse;
//    }

    private ConversationResponse toConversationResponse(Conversation conversation, String userId) {
        ConversationResponse response = conversationMapper.toConversationResponse(conversation);

        log.info("Conversation id: {}", conversation.getId());
        log.info("Current userId: {}", userId);
        log.info("Total participants: {}", conversation.getParticipants().size());

        ParticipantInfo otherParticipant = null;

        for (ParticipantInfo participant : conversation.getParticipants()) {
            log.info("Checking participant: userId={}, username={}",
                    participant.getUserId(),
                    participant.getUsername());

            // bỏ qua chính mình
            if (participant.getUserId().equals(userId)) {
                log.info("→ Skip current user");
                continue;
            }

            // participant còn lại
            otherParticipant = participant;
            log.info("→ Found other participant: {}", participant.getUsername());
            break; // chat 1-1 nên break luôn
        }

        if (otherParticipant != null) {
            response.setConversationName(otherParticipant.getUsername());
            response.setConversationAvatar(otherParticipant.getAvatar());
        } else {
            log.warn("⚠ Không tìm thấy participant còn lại cho conversation {}", conversation.getId());
        }

        return response;
    }

}

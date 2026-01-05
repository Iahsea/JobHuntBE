package vn.hoidanit.jobhunter.domain.response;

import java.time.Instant;
import java.util.List;


import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.hoidanit.jobhunter.domain.ParticipantInfo;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {
    String id;
    String type; // GROUP, DIRECT
    String participantsHash;
    String conversationAvatar;
    String conversationName;
    List<ParticipantInfo> participants;
    Instant createdDate;
    Instant modifiedDate;
}


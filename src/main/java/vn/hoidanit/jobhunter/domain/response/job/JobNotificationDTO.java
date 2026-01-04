package vn.hoidanit.jobhunter.domain.response.job;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.hoidanit.jobhunter.util.constant.JobTypeEnum;
import vn.hoidanit.jobhunter.util.constant.LevelEnum;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobNotificationDTO {
    Long jobId;
    String jobName;
    String companyName;
    String companyLogo;
    String location;
    Double salary;
    LevelEnum level;
    JobTypeEnum jobType;
    List<String> skills;
    Instant createdAt;
    String message;
}


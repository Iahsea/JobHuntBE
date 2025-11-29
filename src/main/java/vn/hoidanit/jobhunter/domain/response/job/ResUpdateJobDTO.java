package vn.hoidanit.jobhunter.domain.response.job;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.util.constant.JobTypeEnum;
import vn.hoidanit.jobhunter.util.constant.LevelEnum;
import vn.hoidanit.jobhunter.util.constant.WorkModeEnum;

@Getter
@Setter
public class ResUpdateJobDTO {
    private long id;
    private String name;

    private String location;

    private double salary;
    private String description;

    private Integer yearsOfExperience;
    private List<WorkModeEnum> workModes;

    private int quantity;

    private JobTypeEnum jobType;

    private LevelEnum level;

    private Instant startDate;
    private Instant endDate;
    private boolean isActive;

    private List<Skill> skills;

    private Instant updatedAt;
    private String updatedBy;
}

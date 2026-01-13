package vn.hoidanit.jobhunter.domain.response.job;

import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.util.constant.JobTypeEnum;
import vn.hoidanit.jobhunter.util.constant.LevelEnum;
import vn.hoidanit.jobhunter.util.constant.WorkModeEnum;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class JobSummaryResponse {
    private long id;
    private String name;
    private String location;
    private double salary;
    private int quantity;
    private JobTypeEnum jobType;
    private LevelEnum level;
    private WorkModeEnum workModes;
    private boolean active;
    private Integer yearsOfExperience;
    private Instant startDate;
    private Instant endDate;
    private List<String> skills;
    private String imageUrl;
//    private List<Skill> skills;
}

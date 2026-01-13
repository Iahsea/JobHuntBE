package vn.hoidanit.jobhunter.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.util.constant.WorkModeEnum;

import java.util.List;

public class JobSpecification {

    public static Specification<Job> hasAnyWorkModes(List<WorkModeEnum> modes) {
        return (root, query, cb) -> {

            // ⚠️ Chỉ apply distinct cho query chính
            if (query.getResultType() != Long.class) {
                query.distinct(true);
            }

            Join<Job, WorkModeEnum> join =
                    root.join("workModes", JoinType.INNER);

            return join.in(modes);
        };
    }

    public static Specification<Job> hasAnySkillNames(List<String> skillNames) {
        return (root, query, cb) -> {

            if (query.getResultType() != Long.class) {
                query.distinct(true);
            }

            Join<Job, Skill> join =
                    root.join("skills", JoinType.INNER);

            return join.get("name").in(skillNames);
        };
    }


}


package vn.hoidanit.jobhunter.specification;

import org.springframework.data.jpa.domain.Specification;
import vn.hoidanit.jobhunter.domain.User;

public class UserSpecification {

    /**
     * Lọc theo roleId (bảng users có field: private Role role;)
     * SELECT * FROM users u
     * JOIN roles r ON u.role_id = r.id
     * WHERE r.id = :roleId
     */
    public static Specification<User> hasRoleId(Long roleId) {
        return (root, query, cb) -> {
            if (roleId == null) {
                // không truyền roleId thì không thêm điều kiện lọc
                return cb.conjunction();
            }
            return cb.equal(root.get("role").get("id"), roleId);
        };
    }

    /**
     * Lọc theo tên role, ví dụ: SUPER_ADMIN, user, hr, hr_vip, user_vip
     */
    public static Specification<User> hasRoleName(String roleName) {
        return (root, query, cb) -> {
            if (roleName == null || roleName.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("role").get("name"), roleName);
        };
    }
}

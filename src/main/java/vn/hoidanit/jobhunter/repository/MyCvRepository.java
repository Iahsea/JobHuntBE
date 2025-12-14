package vn.hoidanit.jobhunter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.hoidanit.jobhunter.domain.MyCv;
import vn.hoidanit.jobhunter.domain.User;

public interface MyCvRepository extends JpaRepository<MyCv, Long> {

    List<MyCv> findByUser(User user);

}

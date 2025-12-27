package vn.hoidanit.jobhunter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.ChatSession;
import vn.hoidanit.jobhunter.domain.User;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long>,
        JpaSpecificationExecutor<ChatSession> {

    List<ChatSession> findByUserOrderByUpdatedAtDesc(User user);
}

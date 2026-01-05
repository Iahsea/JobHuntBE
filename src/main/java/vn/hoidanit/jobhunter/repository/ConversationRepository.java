package vn.hoidanit.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.jobhunter.domain.Conversation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    Optional<Conversation> findByParticipantsHash(String userId);

    @Query("""
        select distinct c
        from Conversation c
        join c.participants p
        where p.userId = :userId
    """)
    List<Conversation> findAllByParticipantIdsContains(@Param("userId") Long userId);

    @Query("select c from Conversation c left join fetch c.participants where c.id = :id")
    Optional<Conversation> findByIdWithParticipants(@Param("id") Long id);
}

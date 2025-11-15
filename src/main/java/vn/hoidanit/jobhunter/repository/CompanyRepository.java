package vn.hoidanit.jobhunter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.Company;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>,
        JpaSpecificationExecutor<Company> {
    @Query("""
        SELECT DISTINCT c
        FROM Company c
        LEFT JOIN FETCH c.jobs
        WHERE c.id = :id
    """)
    Optional<Company> findByIdWithJobs(@Param("id") long id);
}

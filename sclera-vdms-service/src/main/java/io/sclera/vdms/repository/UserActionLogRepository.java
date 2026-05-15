package io.sclera.vdms.repository;

import io.sclera.vdms.model.UserActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActionLogRepository extends JpaRepository<UserActionLog, String> {
    Page<UserActionLog> findByVdmsIdOrderByCreatedAtDesc(String vdmsId, Pageable pageable);
    Page<UserActionLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

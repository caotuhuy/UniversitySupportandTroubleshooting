package com.donga.qlhotro.repository;

import com.donga.qlhotro.entity.SlaConfig;
import com.donga.qlhotro.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaConfigRepository extends JpaRepository<SlaConfig, Long> {
    Optional<SlaConfig> findByPriority(Priority priority);
}

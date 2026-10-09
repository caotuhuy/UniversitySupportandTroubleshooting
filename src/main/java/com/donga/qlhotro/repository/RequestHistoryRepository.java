package com.donga.qlhotro.repository;

import com.donga.qlhotro.entity.RequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {
    List<RequestHistory> findByRequestId(Long requestId);
    List<RequestHistory> findByRequestIdOrderByCreatedAtDesc(Long requestId);
}

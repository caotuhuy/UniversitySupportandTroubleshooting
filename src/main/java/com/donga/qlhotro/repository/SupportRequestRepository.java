package com.donga.qlhotro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.donga.qlhotro.entity.SupportRequest;
import com.donga.qlhotro.enums.Priority;
import com.donga.qlhotro.enums.RequestStatus;

@Repository
public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
    Optional<SupportRequest> findByRequestCode(String requestCode);
    List<SupportRequest> findByRequesterId(Long requesterId);
    List<SupportRequest> findByStatus(RequestStatus status);
    List<SupportRequest> findByPriority(Priority priority);
    long countByStatus(RequestStatus status);

    @Query("SELECT s FROM SupportRequest s WHERE LOWER(s.requestCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<SupportRequest> searchByKeyword(@Param("keyword") String keyword);
}

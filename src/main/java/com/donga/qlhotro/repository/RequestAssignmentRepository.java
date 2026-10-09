package com.donga.qlhotro.repository;

import com.donga.qlhotro.entity.RequestAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestAssignmentRepository extends JpaRepository<RequestAssignment, Long> {
    List<RequestAssignment> findByRequestId(Long requestId);
    List<RequestAssignment> findByTechnicianId(Long technicianId);
}

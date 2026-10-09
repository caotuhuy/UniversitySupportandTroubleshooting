package com.donga.qlhotro.service;

import com.donga.qlhotro.dto.RequestAssignmentDTO;

import java.util.List;

public interface RequestAssignmentService {
    RequestAssignmentDTO assignRequest(Long requestId, Long technicianId, Long assignedById, String note);
    RequestAssignmentDTO getAssignmentById(Long id);
    List<RequestAssignmentDTO> getAssignmentsByTechnician(Long technicianId);
    RequestAssignmentDTO acceptAssignment(Long assignmentId);
    RequestAssignmentDTO completeAssignment(Long assignmentId, String note);
}

package com.donga.qlhotro.service.impl;

import com.donga.qlhotro.dto.CategoryResponseDTO;
import com.donga.qlhotro.dto.DeviceResponseDTO;
import com.donga.qlhotro.dto.RequestAssignmentDTO;
import com.donga.qlhotro.dto.RoomResponseDTO;
import com.donga.qlhotro.dto.SupportRequestResponseDTO;
import com.donga.qlhotro.dto.UserResponseDTO;
import com.donga.qlhotro.entity.RequestAssignment;
import com.donga.qlhotro.entity.SupportRequest;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.RequestAssignmentRepository;
import com.donga.qlhotro.repository.SupportRequestRepository;
import com.donga.qlhotro.repository.UserRepository;
import com.donga.qlhotro.service.RequestAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestAssignmentServiceImpl implements RequestAssignmentService {

    private final RequestAssignmentRepository requestAssignmentRepository;
    private final SupportRequestRepository supportRequestRepository;
    private final UserRepository userRepository;

    public RequestAssignmentServiceImpl(RequestAssignmentRepository requestAssignmentRepository,
                                        SupportRequestRepository supportRequestRepository,
                                        UserRepository userRepository) {
        this.requestAssignmentRepository = requestAssignmentRepository;
        this.supportRequestRepository = supportRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public RequestAssignmentDTO assignRequest(Long requestId, Long technicianId, Long assignedById, String note) {
        SupportRequest request = supportRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", requestId));

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ thuật viên", "id", technicianId));

        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new ResourceNotFoundException("Người giao việc", "id", assignedById));

        RequestAssignment assignment = new RequestAssignment();
        assignment.setRequest(request);
        assignment.setTechnician(technician);
        assignment.setAssignedBy(assignedBy);
        assignment.setNote(note);

        RequestAssignment saved = requestAssignmentRepository.save(assignment);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RequestAssignmentDTO getAssignmentById(Long id) {
        RequestAssignment assignment = requestAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phân công", "id", id));
        return mapToDTO(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestAssignmentDTO> getAssignmentsByTechnician(Long technicianId) {
        return requestAssignmentRepository.findByTechnicianId(technicianId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RequestAssignmentDTO acceptAssignment(Long assignmentId) {
        RequestAssignment assignment = requestAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Phân công", "id", assignmentId));

        assignment.setAcceptedAt(LocalDateTime.now());
        return mapToDTO(requestAssignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public RequestAssignmentDTO completeAssignment(Long assignmentId, String note) {
        RequestAssignment assignment = requestAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Phân công", "id", assignmentId));

        assignment.setCompletedAt(LocalDateTime.now());
        if (note != null) {
            assignment.setNote(note);
        }
        return mapToDTO(requestAssignmentRepository.save(assignment));
    }

    private RequestAssignmentDTO mapToDTO(RequestAssignment assignment) {
        RequestAssignmentDTO dto = new RequestAssignmentDTO();
        dto.setId(assignment.getId());
        if (assignment.getRequest() != null) {
            var req = assignment.getRequest();
            dto.setRequestId(req.getId());
            dto.setRequestCode(req.getRequestCode());
            dto.setRequestTitle(req.getTitle());
            if (req.getStatus() != null) {
                dto.setRequestStatus(req.getStatus().name());
            }
            if (req.getPriority() != null) {
                dto.setRequestPriority(req.getPriority().name());
            }
            if (req.getRequester() != null) {
                dto.setRequesterName(req.getRequester().getFullName());
            }
            if (req.getRoom() != null) {
                dto.setRoomName(req.getRoom().getRoomName());
            }
            dto.setRequest(mapRequestToDTO(req));
        }
        dto.setAssignedAt(assignment.getAssignedAt());
        dto.setAcceptedAt(assignment.getAcceptedAt());
        dto.setCompletedAt(assignment.getCompletedAt());
        dto.setNote(assignment.getNote());

        if (assignment.getTechnician() != null) {
            dto.setTechnician(mapUserToDTO(assignment.getTechnician()));
        }
        if (assignment.getAssignedBy() != null) {
            dto.setAssignedBy(mapUserToDTO(assignment.getAssignedBy()));
        }

        return dto;
    }

    private SupportRequestResponseDTO mapRequestToDTO(SupportRequest req) {
        if (req == null) return null;
        SupportRequestResponseDTO dto = new SupportRequestResponseDTO();
        dto.setId(req.getId());
        dto.setRequestCode(req.getRequestCode());
        dto.setTitle(req.getTitle());
        dto.setDescription(req.getDescription());
        dto.setPriority(req.getPriority());
        dto.setStatus(req.getStatus());
        dto.setCreatedAt(req.getCreatedAt());
        dto.setUpdatedAt(req.getUpdatedAt());
        dto.setCompletedAt(req.getCompletedAt());
        dto.setCancelledAt(req.getCancelledAt());

        if (req.getRequester() != null) {
            dto.setRequester(mapUserToDTO(req.getRequester()));
        }
        if (req.getCategory() != null) {
            CategoryResponseDTO catDto = new CategoryResponseDTO();
            catDto.setId(req.getCategory().getId());
            catDto.setName(req.getCategory().getName());
            dto.setCategory(catDto);
        }
        if (req.getRoom() != null) {
            RoomResponseDTO roomDto = new RoomResponseDTO();
            roomDto.setId(req.getRoom().getId());
            roomDto.setRoomCode(req.getRoom().getRoomCode());
            roomDto.setRoomName(req.getRoom().getRoomName());
            roomDto.setBuilding(req.getRoom().getBuilding());
            roomDto.setFloor(req.getRoom().getFloor());
            dto.setRoom(roomDto);
        }
        if (req.getDevice() != null) {
            DeviceResponseDTO devDto = new DeviceResponseDTO();
            devDto.setId(req.getDevice().getId());
            devDto.setDeviceCode(req.getDevice().getDeviceCode());
            devDto.setDeviceName(req.getDevice().getDeviceName());
            dto.setDevice(devDto);
        }
        return dto;
    }

    private UserResponseDTO mapUserToDTO(User u) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName());
        dto.setEmail(u.getEmail());
        dto.setPhone(u.getPhone());
        dto.setStudentCode(u.getStudentCode());
        dto.setEmployeeCode(u.getEmployeeCode());
        dto.setStatus(u.getStatus());
        return dto;
    }
}

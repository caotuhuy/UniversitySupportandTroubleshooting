package com.donga.qlhotro.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.donga.qlhotro.dto.CategoryResponseDTO;
import com.donga.qlhotro.dto.DeviceResponseDTO;
import com.donga.qlhotro.dto.RoomResponseDTO;
import com.donga.qlhotro.dto.SupportRequestCreateDTO;
import com.donga.qlhotro.dto.SupportRequestResponseDTO;
import com.donga.qlhotro.dto.SupportRequestUpdateDTO;
import com.donga.qlhotro.dto.UserResponseDTO;
import com.donga.qlhotro.entity.Category;
import com.donga.qlhotro.entity.Device;
import com.donga.qlhotro.entity.RequestAssignment;
import com.donga.qlhotro.entity.RequestHistory;
import com.donga.qlhotro.entity.Room;
import com.donga.qlhotro.entity.SupportRequest;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.enums.Priority;
import com.donga.qlhotro.enums.RequestStatus;
import com.donga.qlhotro.exception.InvalidStatusTransitionException;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.CategoryRepository;
import com.donga.qlhotro.repository.DeviceRepository;
import com.donga.qlhotro.repository.RequestAssignmentRepository;
import com.donga.qlhotro.repository.RequestHistoryRepository;
import com.donga.qlhotro.repository.RoomRepository;
import com.donga.qlhotro.repository.SupportRequestRepository;
import com.donga.qlhotro.repository.UserRepository;
import com.donga.qlhotro.security.SecurityUtils;
import com.donga.qlhotro.service.NotificationService;
import com.donga.qlhotro.service.SupportRequestService;

@Service
public class SupportRequestServiceImpl implements SupportRequestService {

    private static final Logger log = LoggerFactory.getLogger(SupportRequestServiceImpl.class);

    private final SupportRequestRepository supportRequestRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final RequestAssignmentRepository requestAssignmentRepository;
    private final RequestHistoryRepository requestHistoryRepository;
    private final NotificationService notificationService;
    private final com.donga.qlhotro.repository.SlaConfigRepository slaConfigRepository;

    public SupportRequestServiceImpl(SupportRequestRepository supportRequestRepository,
                                     UserRepository userRepository,
                                     CategoryRepository categoryRepository,
                                     RoomRepository roomRepository,
                                     DeviceRepository deviceRepository,
                                     RequestAssignmentRepository requestAssignmentRepository,
                                     RequestHistoryRepository requestHistoryRepository,
                                     NotificationService notificationService,
                                     com.donga.qlhotro.repository.SlaConfigRepository slaConfigRepository) {
        this.supportRequestRepository = supportRequestRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.roomRepository = roomRepository;
        this.deviceRepository = deviceRepository;
        this.requestAssignmentRepository = requestAssignmentRepository;
        this.requestHistoryRepository = requestHistoryRepository;
        this.notificationService = notificationService;
        this.slaConfigRepository = slaConfigRepository;
    }

    // =========================================================================
    // CRUD CƠ BẢN
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<SupportRequestResponseDTO> getAllRequests() {
        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        // Nếu là Sinh viên, chỉ được xem các request của chính mình
        if (currentUser.isPresent() && SecurityUtils.isStudent() && !SecurityUtils.isAdmin() && !SecurityUtils.isTechnician()) {
            return supportRequestRepository.findByRequesterId(currentUser.get().getId()).stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        }

        // Nếu là Kỹ thuật viên (và không phải Admin), chỉ xem các request được phân công
        if (currentUser.isPresent() && SecurityUtils.isTechnician() && !SecurityUtils.isAdmin()) {
            Long techId = currentUser.get().getId();
            List<RequestAssignment> assignments = requestAssignmentRepository.findByTechnicianId(techId);
            return assignments.stream()
                    .map(RequestAssignment::getRequest)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        }

        return supportRequestRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SupportRequestResponseDTO getRequestById(Long id) {
        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        // Kiểm tra quyền: Sinh viên chỉ xem được request của chính mình
        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (currentUser.isPresent() && SecurityUtils.isStudent() && !SecurityUtils.isAdmin() && !SecurityUtils.isTechnician()) {
            if (request.getRequester() != null && !request.getRequester().getId().equals(currentUser.get().getId())) {
                throw new AccessDeniedException("Bạn không có quyền xem yêu cầu hỗ trợ của người khác.");
            }
        }

        // Kiểm tra quyền: Kỹ thuật viên chỉ xem được request được phân công cho mình
        if (currentUser.isPresent() && SecurityUtils.isTechnician() && !SecurityUtils.isAdmin()) {
            checkTechnicianAssignment(id, currentUser.get().getId());
        }

        return mapToResponseDTO(request);
    }

    @Override
    @Transactional
    public SupportRequestResponseDTO createRequest(SupportRequestCreateDTO requestDTO) {
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", requestDTO.getCategoryId()));

        // Bảo mật Ownership: Sinh viên không được giả mạo requesterId
        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        User requester;
        if (currentUser.isPresent() && SecurityUtils.isStudent()) {
            requester = currentUser.get();
        } else if (requestDTO.getRequesterId() != null) {
            requester = userRepository.findById(requestDTO.getRequesterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", requestDTO.getRequesterId()));
        } else if (currentUser.isPresent()) {
            requester = currentUser.get();
        } else {
            throw new IllegalArgumentException("Không xác định được người tạo yêu cầu.");
        }

        SupportRequest request = new SupportRequest();
        request.setRequestCode("REQ-" + System.currentTimeMillis() % 1000000);
        request.setTitle(requestDTO.getTitle());
        request.setDescription(requestDTO.getDescription());
        request.setCategory(category);
        request.setRequester(requester);
        request.setPriority(requestDTO.getPriority() != null ? requestDTO.getPriority() : Priority.MEDIUM);
        request.setStatus(RequestStatus.PENDING);

        if (requestDTO.getRoomId() != null) {
            Room room = roomRepository.findById(requestDTO.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", requestDTO.getRoomId()));
            request.setRoom(room);
        }

        if (requestDTO.getDeviceId() != null) {
            Device device = deviceRepository.findById(requestDTO.getDeviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Thiết bị", "id", requestDTO.getDeviceId()));
            request.setDevice(device);
        }

        SupportRequest saved = supportRequestRepository.save(request);
        createHistory(saved, requester, "TẠO_YÊU_CẦU", null, RequestStatus.PENDING.name(), "Yêu cầu đã được tạo mới.");

        // Gửi thông báo cho chính người tạo
        sendNotificationSafe(requester.getId(), "Tạo yêu cầu thành công",
                "Yêu cầu hỗ trợ mã " + saved.getRequestCode() + " đã được gửi thành công và đang chờ tiếp nhận.", "INFO");

        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public SupportRequestResponseDTO updateRequest(Long id, SupportRequestUpdateDTO requestDTO) {
        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        // Kiểm tra quyền sửa: Sinh viên chỉ sửa được request của mình
        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (currentUser.isPresent() && SecurityUtils.isStudent() && !SecurityUtils.isAdmin()) {
            if (request.getRequester() != null && !request.getRequester().getId().equals(currentUser.get().getId())) {
                throw new AccessDeniedException("Bạn không có quyền chỉnh sửa yêu cầu của người khác.");
            }
        }

        // Không cho sửa nếu đã kết thúc
        validateNotFinalized(request, "Không thể chỉnh sửa yêu cầu đã ở trạng thái kết thúc (" + request.getStatus().name() + ").");

        if (requestDTO.getStatus() != null && requestDTO.getStatus() != request.getStatus()) {
            throw new InvalidStatusTransitionException(
                "Không được thay đổi trạng thái trực tiếp qua cập nhật CRUD. Hãy sử dụng endpoint nghiệp vụ tương ứng.");
        }

        if (requestDTO.getTitle() != null) request.setTitle(requestDTO.getTitle());
        if (requestDTO.getDescription() != null) request.setDescription(requestDTO.getDescription());
        if (requestDTO.getPriority() != null) request.setPriority(requestDTO.getPriority());

        if (requestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", requestDTO.getCategoryId()));
            request.setCategory(category);
        }

        if (requestDTO.getRoomId() != null) {
            Room room = roomRepository.findById(requestDTO.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", requestDTO.getRoomId()));
            request.setRoom(room);
        }

        if (requestDTO.getDeviceId() != null) {
            Device device = deviceRepository.findById(requestDTO.getDeviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Thiết bị", "id", requestDTO.getDeviceId()));
            request.setDevice(device);
        }

        SupportRequest updated = supportRequestRepository.save(request);
        User actingUser = currentUser.orElse(request.getRequester());
        createHistory(updated, actingUser, "CẬP_NHẬT_THÔNG_TIN", updated.getStatus().name(), updated.getStatus().name(), "Cập nhật thông tin yêu cầu.");

        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteRequest(Long id) {
        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        // Chỉ Admin mới có quyền xóa vật lý request (Sinh viên phải dùng cancel)
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Chỉ Quản trị viên mới có quyền xóa yêu cầu hỗ trợ.");
        }

        supportRequestRepository.delete(request);
    }

    // =========================================================================
    // TÌM KIẾM & LỌC
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<SupportRequestResponseDTO> searchRequests(String keyword) {
        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        List<SupportRequest> list = supportRequestRepository.searchByKeyword(keyword);

        // Nếu là Sinh viên, lọc chỉ thấy của chính mình
        if (currentUser.isPresent() && SecurityUtils.isStudent() && !SecurityUtils.isAdmin() && !SecurityUtils.isTechnician()) {
            Long myId = currentUser.get().getId();
            return list.stream()
                    .filter(r -> r.getRequester() != null && r.getRequester().getId().equals(myId))
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        }

        // Nếu là Kỹ thuật viên, lọc chỉ thấy yêu cầu mình được phân công
        if (currentUser.isPresent() && SecurityUtils.isTechnician() && !SecurityUtils.isAdmin()) {
            Long techId = currentUser.get().getId();
            java.util.Set<Long> assignedIds = requestAssignmentRepository.findByTechnicianId(techId).stream()
                    .map(a -> a.getRequest() != null ? a.getRequest().getId() : null)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            return list.stream()
                    .filter(r -> assignedIds.contains(r.getId()))
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        }

        return list.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportRequestResponseDTO> filterByStatus(RequestStatus status) {
        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        List<SupportRequest> list = supportRequestRepository.findByStatus(status);

        if (currentUser.isPresent() && SecurityUtils.isStudent() && !SecurityUtils.isAdmin() && !SecurityUtils.isTechnician()) {
            Long myId = currentUser.get().getId();
            return list.stream()
                    .filter(r -> r.getRequester() != null && r.getRequester().getId().equals(myId))
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        }

        if (currentUser.isPresent() && SecurityUtils.isTechnician() && !SecurityUtils.isAdmin()) {
            Long techId = currentUser.get().getId();
            java.util.Set<Long> assignedIds = requestAssignmentRepository.findByTechnicianId(techId).stream()
                    .map(a -> a.getRequest() != null ? a.getRequest().getId() : null)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            return list.stream()
                    .filter(r -> assignedIds.contains(r.getId()))
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        }

        return list.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportRequestResponseDTO> filterByPriority(Priority priority) {
        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        List<SupportRequest> list = supportRequestRepository.findByPriority(priority);

        if (currentUser.isPresent() && SecurityUtils.isStudent() && !SecurityUtils.isAdmin() && !SecurityUtils.isTechnician()) {
            Long myId = currentUser.get().getId();
            return list.stream()
                    .filter(r -> r.getRequester() != null && r.getRequester().getId().equals(myId))
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        }

        if (currentUser.isPresent() && SecurityUtils.isTechnician() && !SecurityUtils.isAdmin()) {
            Long techId = currentUser.get().getId();
            java.util.Set<Long> assignedIds = requestAssignmentRepository.findByTechnicianId(techId).stream()
                    .map(a -> a.getRequest() != null ? a.getRequest().getId() : null)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            return list.stream()
                    .filter(r -> assignedIds.contains(r.getId()))
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        }

        return list.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportRequestResponseDTO> filterByRequester(Long requesterId) {
        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (currentUser.isPresent() && SecurityUtils.isStudent() && !SecurityUtils.isAdmin() && !SecurityUtils.isTechnician()) {
            requesterId = currentUser.get().getId();
        }

        return supportRequestRepository.findByRequesterId(requesterId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // NGHIỆP VỤ — CHUYỂN TRẠNG THÁI (CÓ KIỂM TRA HỢP LỆ & NOTIFICATION)
    // =========================================================================

    /**
     * PENDING → RECEIVED
     * Admin tiếp nhận yêu cầu mới.
     */
    @Override
    @Transactional
    public SupportRequestResponseDTO receiveRequest(Long id, Long userId) {
        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        User user = currentUser.orElseGet(() -> userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId)));

        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Chỉ Quản trị viên mới có quyền tiếp nhận yêu cầu.");
        }

        validateTransition(request, RequestStatus.RECEIVED, RequestStatus.PENDING);

        String oldStatus = request.getStatus().name();
        request.setStatus(RequestStatus.RECEIVED);
        SupportRequest updated = supportRequestRepository.save(request);
        createHistory(updated, user, "TIẾP_NHẬN_YÊU_CẦU", oldStatus, RequestStatus.RECEIVED.name(), "Đã tiếp nhận yêu cầu.");

        // Thông báo cho sinh viên
        if (updated.getRequester() != null) {
            sendNotificationSafe(updated.getRequester().getId(), "Yêu cầu đã được tiếp nhận",
                    "Yêu cầu mã " + updated.getRequestCode() + " đã được bộ phận hỗ trợ tiếp nhận và đang chờ phân công kỹ thuật viên.", "INFO");
        }

        return mapToResponseDTO(updated);
    }

    /**
     * PENDING → REJECTED
     * Admin từ chối yêu cầu.
     */
    @Override
    @Transactional
    public SupportRequestResponseDTO rejectRequest(Long id, Long userId, String reason) {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Chỉ Quản trị viên mới có quyền từ chối yêu cầu.");
        }

        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        User user = currentUser.orElseGet(() -> userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId)));

        validateTransition(request, RequestStatus.REJECTED, RequestStatus.PENDING);

        String oldStatus = request.getStatus().name();
        request.setStatus(RequestStatus.REJECTED);
        SupportRequest updated = supportRequestRepository.save(request);
        createHistory(updated, user, "TỪ_CHỐI_YÊU_CẦU", oldStatus, RequestStatus.REJECTED.name(), reason);

        // Thông báo cho sinh viên
        if (updated.getRequester() != null) {
            sendNotificationSafe(updated.getRequester().getId(), "Yêu cầu bị từ chối",
                    "Yêu cầu mã " + updated.getRequestCode() + " đã bị từ chối. Lý do: " + (reason != null ? reason : "Không có"), "DANGER");
        }

        return mapToResponseDTO(updated);
    }

    /**
     * PENDING / RECEIVED / ASSIGNED / IN_PROGRESS / WAITING_INFO → CANCELLED
     * Hủy yêu cầu (soft cancel). Không xóa vật lý record.
     */
    @Override
    @Transactional
    public SupportRequestResponseDTO cancelRequest(Long id, Long userId, String reason) {
        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        User user = currentUser.orElseGet(() -> userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId)));

        // Nếu là Sinh viên, kiểm tra ownership
        if (currentUser.isPresent() && SecurityUtils.isStudent() && !SecurityUtils.isAdmin()) {
            if (request.getRequester() != null && !request.getRequester().getId().equals(currentUser.get().getId())) {
                throw new AccessDeniedException("Bạn không có quyền hủy yêu cầu của người khác.");
            }
        }

        validateTransition(request, RequestStatus.CANCELLED,
                RequestStatus.PENDING, RequestStatus.RECEIVED, RequestStatus.ASSIGNED,
                RequestStatus.IN_PROGRESS, RequestStatus.WAITING_INFO);

        String oldStatus = request.getStatus().name();
        request.setStatus(RequestStatus.CANCELLED);
        request.setCancelledAt(LocalDateTime.now());
        SupportRequest updated = supportRequestRepository.save(request);
        createHistory(updated, user, "HỦY_YÊU_CẦU", oldStatus, RequestStatus.CANCELLED.name(), reason);

        // Thông báo
        if (currentUser.isPresent() && SecurityUtils.isAdmin() && updated.getRequester() != null) {
            sendNotificationSafe(updated.getRequester().getId(), "Yêu cầu đã bị hủy",
                    "Yêu cầu mã " + updated.getRequestCode() + " đã bị hủy bởi quản trị viên. Lý do: " + reason, "WARNING");
        }

        return mapToResponseDTO(updated);
    }

    /**
     * RECEIVED → ASSIGNED
     * Admin phân công kỹ thuật viên xử lý yêu cầu.
     */
    @Override
    @Transactional
    public SupportRequestResponseDTO assignRequest(Long id, Long technicianId, Long assignedById, String note) {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Chỉ Quản trị viên mới có quyền phân công xử lý yêu cầu.");
        }

        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ thuật viên", "id", technicianId));

        boolean hasTechRole = technician.getRoles() != null && technician.getRoles().stream()
                .anyMatch(r -> r.getName() == com.donga.qlhotro.enums.RoleName.ROLE_TECHNICIAN);
        if (!hasTechRole) {
            throw new IllegalArgumentException("Người dùng được chỉ định không có vai trò Kỹ thuật viên.");
        }

        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        User assignedBy = currentUser.orElseGet(() -> userRepository.findById(assignedById)
                .orElseThrow(() -> new ResourceNotFoundException("Người giao việc", "id", assignedById)));

        validateTransition(request, RequestStatus.ASSIGNED, RequestStatus.RECEIVED);

        String oldStatus = request.getStatus().name();
        request.setStatus(RequestStatus.ASSIGNED);
        SupportRequest updated = supportRequestRepository.save(request);

        RequestAssignment assignment = new RequestAssignment();
        assignment.setRequest(updated);
        assignment.setTechnician(technician);
        assignment.setAssignedBy(assignedBy);
        assignment.setNote(note);
        requestAssignmentRepository.save(assignment);

        createHistory(updated, assignedBy, "PHÂN_CÔNG_XỬ_LÝ", oldStatus, RequestStatus.ASSIGNED.name(),
                "Phân công cho " + technician.getFullName() + (note != null && !note.isBlank() ? ". Ghi chú: " + note : ""));

        // Thông báo cho kỹ thuật viên
        sendNotificationSafe(technician.getId(), "Nhiệm vụ xử lý mới",
                "Bạn đã được phân công xử lý yêu cầu mã " + updated.getRequestCode() + ": " + updated.getTitle(), "ASSIGNMENT");

        // Thông báo cho người gửi yêu cầu
        if (updated.getRequester() != null) {
            sendNotificationSafe(updated.getRequester().getId(), "Yêu cầu đã được phân công",
                    "Yêu cầu mã " + updated.getRequestCode() + " đã được giao cho kỹ thuật viên " + technician.getFullName() + " xử lý.", "INFO");
        }

        return mapToResponseDTO(updated);
    }

    /**
     * ASSIGNED / IN_PROGRESS / WAITING_INFO → ASSIGNED
     * Chuyển người xử lý (Reassign - 4.7).
     */
    @Override
    @Transactional
    public SupportRequestResponseDTO reassignRequest(Long id, Long newTechnicianId, Long assignedById, String note) {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Chỉ Quản trị viên mới có quyền chuyển người xử lý.");
        }

        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        validateNotFinalized(request, "Không thể chuyển kỹ thuật viên cho yêu cầu đã " + request.getStatus().name());

        if (request.getStatus() == RequestStatus.PENDING || request.getStatus() == RequestStatus.RECEIVED) {
            throw new InvalidStatusTransitionException("Yêu cầu chưa được phân công trước đó, vui lòng sử dụng chức năng phân công.");
        }

        User newTechnician = userRepository.findById(newTechnicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Kỹ thuật viên mới", "id", newTechnicianId));

        boolean hasTechRole = newTechnician.getRoles() != null && newTechnician.getRoles().stream()
                .anyMatch(r -> r.getName() == com.donga.qlhotro.enums.RoleName.ROLE_TECHNICIAN);
        if (!hasTechRole) {
            throw new IllegalArgumentException("Người dùng được chọn không có vai trò Kỹ thuật viên.");
        }

        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        User assignedBy = currentUser.orElseGet(() -> userRepository.findById(assignedById)
                .orElseThrow(() -> new ResourceNotFoundException("Người giao việc", "id", assignedById)));

        // Đóng các assignment đang mở trước đó
        List<RequestAssignment> oldAssignments = requestAssignmentRepository.findByRequestId(id);
        String oldTechName = "Kỹ thuật viên tiền nhiệm";
        for (RequestAssignment old : oldAssignments) {
            if (old.getCompletedAt() == null) {
                old.setCompletedAt(LocalDateTime.now());
                if (old.getTechnician() != null) {
                    oldTechName = old.getTechnician().getFullName();
                }
                old.setNote((old.getNote() != null && !old.getNote().isBlank() ? old.getNote() + " | " : "") + "Chuyển giao cho " + newTechnician.getFullName());
                requestAssignmentRepository.save(old);
            }
        }

        RequestAssignment newAssignment = new RequestAssignment();
        newAssignment.setRequest(request);
        newAssignment.setTechnician(newTechnician);
        newAssignment.setAssignedBy(assignedBy);
        newAssignment.setNote(note);
        requestAssignmentRepository.save(newAssignment);

        String oldStatus = request.getStatus().name();
        request.setStatus(RequestStatus.ASSIGNED);
        SupportRequest updated = supportRequestRepository.save(request);

        createHistory(updated, assignedBy, "CHUYỂN_NGƯỜI_XỬ_LÝ", oldStatus, RequestStatus.ASSIGNED.name(),
                "Chuyển từ " + oldTechName + " sang " + newTechnician.getFullName() + (note != null && !note.isBlank() ? ". Ghi chú: " + note : ""));

        sendNotificationSafe(newTechnician.getId(), "Tiếp nhận yêu cầu chuyển giao",
                "Bạn được phân công tiếp nhận yêu cầu mã " + updated.getRequestCode() + ": " + updated.getTitle(), "ASSIGNMENT");

        if (updated.getRequester() != null) {
            sendNotificationSafe(updated.getRequester().getId(), "Yêu cầu đã đổi kỹ thuật viên",
                    "Yêu cầu mã " + updated.getRequestCode() + " đã được chuyển giao cho kỹ thuật viên " + newTechnician.getFullName() + " tiếp tục xử lý.", "INFO");
        }

        return mapToResponseDTO(updated);
    }

    /**
     * ASSIGNED → IN_PROGRESS
     * Kỹ thuật viên chấp nhận và bắt đầu xử lý.
     */
    @Override
    @Transactional
    public SupportRequestResponseDTO acceptRequest(Long id, Long technicianId) {
        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (currentUser.isPresent() && SecurityUtils.isTechnician() && !SecurityUtils.isAdmin()) {
            technicianId = currentUser.get().getId();
        }

        // Kiểm tra Technician Ownership
        checkTechnicianAssignment(id, technicianId);

        final Long finalTechId = technicianId;
        User technician = currentUser.filter(u -> u.getId().equals(finalTechId))
                .orElseGet(() -> userRepository.findById(finalTechId)
                        .orElseThrow(() -> new ResourceNotFoundException("Kỹ thuật viên", "id", finalTechId)));

        // Xử lý idempotent: Nếu yêu cầu đã ở trạng thái IN_PROGRESS thì trả về DTO thành công, không gọi transition, không duplicate history/notification
        if (request.getStatus() == RequestStatus.IN_PROGRESS) {
            return mapToResponseDTO(request);
        }

        validateTransition(request, RequestStatus.IN_PROGRESS, RequestStatus.ASSIGNED);

        String oldStatus = request.getStatus().name();
        request.setStatus(RequestStatus.IN_PROGRESS);
        SupportRequest updated = supportRequestRepository.save(request);

        // Đánh dấu acceptedAt cho assignment tương ứng
        List<RequestAssignment> assignments = requestAssignmentRepository.findByRequestId(id);
        for (RequestAssignment assignment : assignments) {
            if (assignment.getTechnician().getId().equals(technicianId) && assignment.getAcceptedAt() == null) {
                assignment.setAcceptedAt(LocalDateTime.now());
                requestAssignmentRepository.save(assignment);
            }
        }

        createHistory(updated, technician, "BẮT_ĐẦU_XỬ_LÝ", oldStatus, RequestStatus.IN_PROGRESS.name(), "Kỹ thuật viên đã nhận xử lý.");

        // Thông báo cho sinh viên
        if (updated.getRequester() != null) {
            sendNotificationSafe(updated.getRequester().getId(), "Kỹ thuật viên bắt đầu xử lý",
                    "Kỹ thuật viên " + technician.getFullName() + " đã tiếp nhận và bắt đầu xử lý yêu cầu " + updated.getRequestCode() + ".", "INFO");
        }

        return mapToResponseDTO(updated);
    }

    /**
     * IN_PROGRESS — Cập nhật tiến độ xử lý (không đổi trạng thái).
     */
    @Override
    @Transactional
    public SupportRequestResponseDTO updateProgress(Long id, Long userId, String note) {
        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (currentUser.isPresent() && SecurityUtils.isTechnician() && !SecurityUtils.isAdmin()) {
            userId = currentUser.get().getId();
        }

        // Kiểm tra Technician Ownership nếu là Technician
        if (SecurityUtils.isTechnician()) {
            checkTechnicianAssignment(id, userId);
        }

        final Long progressUserId = userId;
        User user = currentUser.orElseGet(() -> userRepository.findById(progressUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", progressUserId)));

        if (request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new InvalidStatusTransitionException(
                    "Chỉ có thể cập nhật tiến độ khi yêu cầu đang ở trạng thái IN_PROGRESS. Trạng thái hiện tại: " + request.getStatus().name());
        }

        createHistory(request, user, "CẬP_NHẬT_TIẾN_ĐỘ", RequestStatus.IN_PROGRESS.name(), RequestStatus.IN_PROGRESS.name(), note);

        // Thông báo cho người gửi yêu cầu
        if (request.getRequester() != null) {
            sendNotificationSafe(request.getRequester().getId(), "Cập nhật tiến độ xử lý",
                    "Yêu cầu mã " + request.getRequestCode() + " có tiến độ mới: " + note, "INFO");
        }

        return mapToResponseDTO(request);
    }

    /**
     * IN_PROGRESS → WAITING_INFO
     * Yêu cầu thêm thông tin từ người gửi.
     */
    @Override
    @Transactional
    public SupportRequestResponseDTO requestMoreInformation(Long id, Long userId, String note) {
        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (currentUser.isPresent() && SecurityUtils.isTechnician() && !SecurityUtils.isAdmin()) {
            userId = currentUser.get().getId();
        }

        if (SecurityUtils.isTechnician()) {
            checkTechnicianAssignment(id, userId);
        }

        final Long needInfoUserId = userId;
        User user = currentUser.orElseGet(() -> userRepository.findById(needInfoUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", needInfoUserId)));

        validateTransition(request, RequestStatus.WAITING_INFO, RequestStatus.IN_PROGRESS);

        String oldStatus = request.getStatus().name();
        request.setStatus(RequestStatus.WAITING_INFO);
        SupportRequest updated = supportRequestRepository.save(request);
        createHistory(updated, user, "YÊU_CẦU_THÊM_THÔNG_TIN", oldStatus, RequestStatus.WAITING_INFO.name(), note);

        // Thông báo cho sinh viên
        if (updated.getRequester() != null) {
            sendNotificationSafe(updated.getRequester().getId(), "Cần bổ sung thông tin",
                    "Yêu cầu mã " + updated.getRequestCode() + " cần bạn bổ sung thêm thông tin: " + note, "WARNING");
        }

        return mapToResponseDTO(updated);
    }

    /**
     * WAITING_INFO → IN_PROGRESS
     * Tiếp tục xử lý sau khi người dùng cung cấp thêm thông tin.
     */
    @Override
    @Transactional
    public SupportRequestResponseDTO resumeRequest(Long id, Long userId, String note) {
        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        User user = currentUser.orElseGet(() -> userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", userId)));

        // Nếu là Sinh viên, kiểm tra ownership
        if (currentUser.isPresent() && SecurityUtils.isStudent() && !SecurityUtils.isAdmin()) {
            if (request.getRequester() != null && !request.getRequester().getId().equals(currentUser.get().getId())) {
                throw new AccessDeniedException("Bạn không có quyền thao tác trên yêu cầu này.");
            }
        }

        validateTransition(request, RequestStatus.IN_PROGRESS, RequestStatus.WAITING_INFO);

        String oldStatus = request.getStatus().name();
        request.setStatus(RequestStatus.IN_PROGRESS);
        SupportRequest updated = supportRequestRepository.save(request);
        createHistory(updated, user, "TIẾP_TỤC_XỬ_LÝ", oldStatus, RequestStatus.IN_PROGRESS.name(),
                note != null && !note.isBlank() ? note : "Tiếp tục xử lý sau khi nhận thêm thông tin.");

        // Thông báo cho kỹ thuật viên được phân công
        List<RequestAssignment> assignments = requestAssignmentRepository.findByRequestId(id);
        for (RequestAssignment assignment : assignments) {
            if (assignment.getTechnician() != null) {
                sendNotificationSafe(assignment.getTechnician().getId(), "Thông tin bổ sung đã cập nhật",
                        "Người gửi đã phản hồi/bổ sung thông tin cho yêu cầu mã " + updated.getRequestCode() + ".", "INFO");
            }
        }

        return mapToResponseDTO(updated);
    }

    /**
     * IN_PROGRESS → COMPLETED
     * Hoàn thành yêu cầu.
     */
    @Override
    @Transactional
    public SupportRequestResponseDTO completeRequest(Long id, Long userId, String note) {
        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (currentUser.isPresent() && SecurityUtils.isTechnician() && !SecurityUtils.isAdmin()) {
            userId = currentUser.get().getId();
        }

        if (SecurityUtils.isTechnician()) {
            checkTechnicianAssignment(id, userId);
        }

        final Long completeUserId = userId;
        User user = currentUser.orElseGet(() -> userRepository.findById(completeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", completeUserId)));

        validateTransition(request, RequestStatus.COMPLETED, RequestStatus.IN_PROGRESS);

        String oldStatus = request.getStatus().name();
        request.setStatus(RequestStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());
        SupportRequest updated = supportRequestRepository.save(request);

        // Đánh dấu completedAt cho tất cả assignment đang mở
        List<RequestAssignment> assignments = requestAssignmentRepository.findByRequestId(id);
        for (RequestAssignment assignment : assignments) {
            if (assignment.getCompletedAt() == null) {
                assignment.setCompletedAt(LocalDateTime.now());
                requestAssignmentRepository.save(assignment);
            }
        }

        createHistory(updated, user, "HOÀN_THÀNH_YÊU_CẦU", oldStatus, RequestStatus.COMPLETED.name(), note);

        // Thông báo cho người gửi yêu cầu
        if (updated.getRequester() != null) {
            sendNotificationSafe(updated.getRequester().getId(), "Yêu cầu đã hoàn thành",
                    "Yêu cầu mã " + updated.getRequestCode() + " đã được xử lý hoàn thành. Vui lòng kiểm tra và đánh giá dịch vụ hỗ trợ!", "SUCCESS");
        }

        return mapToResponseDTO(updated);
    }

    /**
     * Cập nhật mức độ ưu tiên.
     */
    @Override
    @Transactional
    public SupportRequestResponseDTO updatePriority(Long id, Priority priority) {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Chỉ Quản trị viên mới có quyền thay đổi mức độ ưu tiên.");
        }

        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        validateNotFinalized(request, "Không thể thay đổi độ ưu tiên của yêu cầu đã " + request.getStatus().name());

        request.setPriority(priority);
        return mapToResponseDTO(supportRequestRepository.save(request));
    }

    /**
     * Cập nhật danh mục.
     */
    @Override
    @Transactional
    public SupportRequestResponseDTO updateCategory(Long id, Long categoryId) {
        if (!SecurityUtils.isAdmin()) {
            throw new AccessDeniedException("Chỉ Quản trị viên mới có quyền thay đổi danh mục yêu cầu.");
        }

        SupportRequest request = supportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hỗ trợ", "id", id));

        validateNotFinalized(request, "Không thể thay đổi danh mục của yêu cầu đã " + request.getStatus().name());

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", categoryId));
        request.setCategory(category);
        return mapToResponseDTO(supportRequestRepository.save(request));
    }

    // =========================================================================
    // HELPER — KIỂM TRA BẢO MẬT & ASSIGNMENT
    // =========================================================================

    private void checkTechnicianAssignment(Long requestId, Long technicianId) {
        if (SecurityUtils.isAdmin()) {
            return; // Admin có toàn quyền
        }
        List<RequestAssignment> assignments = requestAssignmentRepository.findByRequestId(requestId);
        boolean isAssigned = assignments.stream()
                .anyMatch(a -> a.getTechnician() != null && a.getTechnician().getId().equals(technicianId));
        if (!isAssigned) {
            throw new AccessDeniedException("Kỹ thuật viên không được phân công xử lý yêu cầu này.");
        }
    }

    private void sendNotificationSafe(Long userId, String title, String content, String type) {
        if (userId == null) return;
        try {
            notificationService.createNotification(userId, title, content, type);
        } catch (Exception e) {
            log.warn("Không thể tạo thông báo tự động cho userId {}: {}", userId, e.getMessage());
        }
    }

    private void validateTransition(SupportRequest request, RequestStatus target, RequestStatus... allowedFrom) {
        RequestStatus current = request.getStatus();
        for (RequestStatus allowed : allowedFrom) {
            if (current == allowed) {
                return; // hợp lệ
            }
        }
        throw new InvalidStatusTransitionException(current.name(), target.name());
    }

    private void validateNotFinalized(SupportRequest request, String message) {
        RequestStatus s = request.getStatus();
        if (s == RequestStatus.COMPLETED || s == RequestStatus.CANCELLED || s == RequestStatus.REJECTED) {
            throw new InvalidStatusTransitionException(message);
        }
    }

    private void createHistory(SupportRequest request, User user, String action,
                               String oldStatus, String newStatus, String note) {
        RequestHistory history = new RequestHistory();
        history.setRequest(request);
        history.setUser(user);
        history.setAction(action);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setNote(note);
        requestHistoryRepository.save(history);
    }

    private SupportRequestResponseDTO mapToResponseDTO(SupportRequest request) {
        SupportRequestResponseDTO dto = new SupportRequestResponseDTO();
        dto.setId(request.getId());
        dto.setRequestCode(request.getRequestCode());
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setPriority(request.getPriority());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.setCompletedAt(request.getCompletedAt());
        dto.setCancelledAt(request.getCancelledAt());

        if (request.getCategory() != null) {
            Category c = request.getCategory();
            CategoryResponseDTO cDto = new CategoryResponseDTO();
            cDto.setId(c.getId());
            cDto.setName(c.getName());
            cDto.setDescription(c.getDescription());
            cDto.setStatus(c.getStatus());
            dto.setCategory(cDto);
        }

        if (request.getRequester() != null) {
            User u = request.getRequester();
            UserResponseDTO uDto = new UserResponseDTO();
            uDto.setId(u.getId());
            uDto.setUsername(u.getUsername());
            uDto.setFullName(u.getFullName());
            uDto.setEmail(u.getEmail());
            uDto.setPhone(u.getPhone());
            uDto.setStudentCode(u.getStudentCode());
            uDto.setEmployeeCode(u.getEmployeeCode());
            uDto.setStatus(u.getStatus());
            dto.setRequester(uDto);
        }

        if (request.getRoom() != null) {
            Room r = request.getRoom();
            RoomResponseDTO rDto = new RoomResponseDTO();
            rDto.setId(r.getId());
            rDto.setRoomCode(r.getRoomCode());
            rDto.setRoomName(r.getRoomName());
            rDto.setBuilding(r.getBuilding());
            rDto.setFloor(r.getFloor());
            rDto.setStatus(r.getStatus());
            dto.setRoom(rDto);
        }

        if (request.getDevice() != null) {
            Device d = request.getDevice();
            DeviceResponseDTO dDto = new DeviceResponseDTO();
            dDto.setId(d.getId());
            dDto.setDeviceCode(d.getDeviceCode());
            dDto.setDeviceName(d.getDeviceName());
            dDto.setDeviceType(d.getDeviceType());
            dDto.setStatus(d.getStatus());
            dto.setDevice(dDto);
        }

        // Lấy thông tin Kỹ thuật viên phụ trách gần nhất
        try {
            List<RequestAssignment> assignments = requestAssignmentRepository.findByRequestId(request.getId());
            if (assignments != null && !assignments.isEmpty()) {
                RequestAssignment latest = assignments.get(assignments.size() - 1);
                if (latest.getTechnician() != null) {
                    User t = latest.getTechnician();
                    UserResponseDTO tDto = new UserResponseDTO();
                    tDto.setId(t.getId());
                    tDto.setUsername(t.getUsername());
                    tDto.setFullName(t.getFullName());
                    tDto.setEmail(t.getEmail());
                    tDto.setPhone(t.getPhone());
                    dto.setCurrentTechnician(tDto);
                }
            }
        } catch (Exception ignored) {
        }

        // Tính toán hạn định thời gian theo SLA (SLA Tracking & Overdue Calculation)
        if (request.getPriority() != null && request.getCreatedAt() != null) {
            try {
                slaConfigRepository.findByPriority(request.getPriority()).ifPresent(sla -> {
                    if (sla.getResolutionTime() != null && sla.getResolutionTime() > 0) {
                        LocalDateTime deadline = request.getCreatedAt().plusHours(sla.getResolutionTime());
                        dto.setResolutionDeadline(deadline);

                        boolean isTerminal = (request.getStatus() == RequestStatus.CANCELLED || request.getStatus() == RequestStatus.REJECTED);
                        if (isTerminal) {
                            dto.setIsOverdue(false);
                            dto.setSlaStatus("TERMINATED");
                            dto.setSlaStatusText("Đã đóng");
                        } else {
                            LocalDateTime compareTime = (request.getStatus() == RequestStatus.COMPLETED && request.getCompletedAt() != null)
                                    ? request.getCompletedAt()
                                    : LocalDateTime.now();

                            if (compareTime.isAfter(deadline)) {
                                dto.setIsOverdue(true);
                                dto.setSlaStatus("OVERDUE");
                                dto.setSlaStatusText("Quá hạn");
                            } else {
                                int warnHours = (sla.getWarningTime() != null) ? sla.getWarningTime() : 2;
                                LocalDateTime warnThreshold = deadline.minusHours(warnHours);
                                if (compareTime.isAfter(warnThreshold)) {
                                    dto.setIsOverdue(false);
                                    dto.setSlaStatus("WARNING");
                                    dto.setSlaStatusText("Sắp quá hạn");
                                } else {
                                    dto.setIsOverdue(false);
                                    dto.setSlaStatus("ON_TIME");
                                    dto.setSlaStatusText("Trong hạn");
                                }
                            }
                        }
                    }
                });
            } catch (Exception ignored) {
            }
        }

        return dto;
    }
}

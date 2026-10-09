package com.donga.qlhotro.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.donga.qlhotro.dto.DashboardStatisticsDTO;
import com.donga.qlhotro.entity.RequestAssignment;
import com.donga.qlhotro.entity.SlaConfig;
import com.donga.qlhotro.entity.SupportRequest;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.enums.Priority;
import com.donga.qlhotro.enums.RequestStatus;
import com.donga.qlhotro.repository.RequestAssignmentRepository;
import com.donga.qlhotro.repository.SlaConfigRepository;
import com.donga.qlhotro.repository.SupportRequestRepository;
import com.donga.qlhotro.repository.UserRepository;
import com.donga.qlhotro.security.SecurityUtils;
import com.donga.qlhotro.service.DashboardService;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final SupportRequestRepository supportRequestRepository;
    private final RequestAssignmentRepository requestAssignmentRepository;
    private final UserRepository userRepository;
    private final SlaConfigRepository slaConfigRepository;

    public DashboardServiceImpl(SupportRequestRepository supportRequestRepository,
                                RequestAssignmentRepository requestAssignmentRepository,
                                UserRepository userRepository,
                                SlaConfigRepository slaConfigRepository) {
        this.supportRequestRepository = supportRequestRepository;
        this.requestAssignmentRepository = requestAssignmentRepository;
        this.userRepository = userRepository;
        this.slaConfigRepository = slaConfigRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatisticsDTO getStatistics() {
        Optional<User> currentUser = SecurityUtils.getCurrentUser(userRepository);

        // Trường hợp 1: SINH VIÊN -> Chỉ đếm các request của chính mình
        if (currentUser.isPresent() && SecurityUtils.isStudent() && !SecurityUtils.isAdmin() && !SecurityUtils.isTechnician()) {
            Long studentId = currentUser.get().getId();
            List<SupportRequest> myRequests = supportRequestRepository.findByRequesterId(studentId);

            DashboardStatisticsDTO stats = new DashboardStatisticsDTO();
            stats.setTotalRequests(myRequests.size());
            stats.setPendingRequests(myRequests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count());
            stats.setReceivedRequests(myRequests.stream().filter(r -> r.getStatus() == RequestStatus.RECEIVED).count());
            stats.setAssignedRequests(myRequests.stream().filter(r -> r.getStatus() == RequestStatus.ASSIGNED).count());
            stats.setInProgressRequests(myRequests.stream().filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS).count());
            stats.setWaitingInfoRequests(myRequests.stream().filter(r -> r.getStatus() == RequestStatus.WAITING_INFO).count());
            stats.setCompletedRequests(myRequests.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count());
            stats.setRejectedRequests(myRequests.stream().filter(r -> r.getStatus() == RequestStatus.REJECTED).count());
            stats.setCancelledRequests(myRequests.stream().filter(r -> r.getStatus() == RequestStatus.CANCELLED).count());

            populateExtendedMetrics(stats, myRequests);
            return stats;
        }

        // Trường hợp 2: KỸ THUẬT VIÊN -> Đếm theo nhiệm vụ được phân công
        if (currentUser.isPresent() && SecurityUtils.isTechnician() && !SecurityUtils.isAdmin()) {
            Long techId = currentUser.get().getId();
            List<RequestAssignment> myAssignments = requestAssignmentRepository.findByTechnicianId(techId);
            List<SupportRequest> techRequests = myAssignments.stream()
                    .map(RequestAssignment::getRequest)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            DashboardStatisticsDTO stats = new DashboardStatisticsDTO();
            stats.setTotalRequests(myAssignments.size());
            stats.setPendingRequests(myAssignments.stream().filter(a -> a.getRequest() != null && a.getRequest().getStatus() == RequestStatus.ASSIGNED).count());
            stats.setReceivedRequests(0L);
            stats.setAssignedRequests(myAssignments.stream().filter(a -> a.getRequest() != null && a.getRequest().getStatus() == RequestStatus.ASSIGNED).count());
            stats.setInProgressRequests(myAssignments.stream().filter(a -> a.getRequest() != null && a.getRequest().getStatus() == RequestStatus.IN_PROGRESS).count());
            stats.setWaitingInfoRequests(myAssignments.stream().filter(a -> a.getRequest() != null && a.getRequest().getStatus() == RequestStatus.WAITING_INFO).count());
            stats.setCompletedRequests(myAssignments.stream().filter(a -> a.getRequest() != null && a.getRequest().getStatus() == RequestStatus.COMPLETED).count());
            stats.setRejectedRequests(0L);
            stats.setCancelledRequests(myAssignments.stream().filter(a -> a.getRequest() != null && a.getRequest().getStatus() == RequestStatus.CANCELLED).count());

            populateExtendedMetrics(stats, techRequests);
            return stats;
        }

        // Trường hợp 3: ADMIN / Mặc định -> Thống kê toàn hệ thống
        List<SupportRequest> allRequests = supportRequestRepository.findAll();

        DashboardStatisticsDTO statistics = new DashboardStatisticsDTO();
        statistics.setTotalRequests(allRequests.size());
        statistics.setPendingRequests(countByStatus(RequestStatus.PENDING));
        statistics.setReceivedRequests(countByStatus(RequestStatus.RECEIVED));
        statistics.setAssignedRequests(countByStatus(RequestStatus.ASSIGNED));
        statistics.setInProgressRequests(countByStatus(RequestStatus.IN_PROGRESS));
        statistics.setWaitingInfoRequests(countByStatus(RequestStatus.WAITING_INFO));
        statistics.setCompletedRequests(countByStatus(RequestStatus.COMPLETED));
        statistics.setRejectedRequests(countByStatus(RequestStatus.REJECTED));
        statistics.setCancelledRequests(countByStatus(RequestStatus.CANCELLED));

        populateExtendedMetrics(statistics, allRequests);

        // Thống kê theo kỹ thuật viên từ assignments
        Map<String, Long> techCount = requestAssignmentRepository.findAll().stream()
                .filter(a -> a.getTechnician() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getTechnician().getFullName() != null ? a.getTechnician().getFullName() : a.getTechnician().getUsername(),
                        Collectors.counting()
                ));
        statistics.setRequestsByTechnician(techCount);

        return statistics;
    }

    private void populateExtendedMetrics(DashboardStatisticsDTO stats, List<SupportRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        // Danh mục
        Map<String, Long> categoryMap = requests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCategory() != null ? r.getCategory().getName() : "Chưa phân loại",
                        Collectors.counting()
                ));
        stats.setRequestsByCategory(categoryMap);

        // Mức độ ưu tiên
        Map<String, Long> priorityMap = requests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getPriority() != null ? r.getPriority().name() : "MEDIUM",
                        Collectors.counting()
                ));
        stats.setRequestsByPriority(priorityMap);

        // SLA
        Map<Priority, SlaConfig> slaMap = slaConfigRepository.findAll().stream()
                .collect(Collectors.toMap(SlaConfig::getPriority, s -> s, (s1, s2) -> s1));

        LocalDateTime now = LocalDateTime.now();
        long overdue = 0;
        long warning = 0;
        long completedOnTime = 0;
        long totalCompleted = 0;

        for (SupportRequest req : requests) {
            if (req.getStatus() == RequestStatus.CANCELLED || req.getStatus() == RequestStatus.REJECTED) {
                continue;
            }
            Priority prio = req.getPriority() != null ? req.getPriority() : Priority.MEDIUM;
            SlaConfig sla = slaMap.get(prio);
            int resolutionHours = (sla != null && sla.getResolutionTime() != null) ? sla.getResolutionTime() : 24;
            int warningHours = (sla != null && sla.getWarningTime() != null) ? sla.getWarningTime() : 2;

            LocalDateTime deadline = req.getCreatedAt() != null ? req.getCreatedAt().plusHours(resolutionHours) : null;
            if (deadline == null) continue;

            if (req.getStatus() == RequestStatus.COMPLETED) {
                totalCompleted++;
                LocalDateTime completedAt = req.getCompletedAt() != null ? req.getCompletedAt() : req.getUpdatedAt();
                if (completedAt != null && completedAt.isAfter(deadline)) {
                    overdue++;
                } else {
                    completedOnTime++;
                }
            } else {
                if (now.isAfter(deadline)) {
                    overdue++;
                } else if (deadline.minusHours(warningHours).isBefore(now)) {
                    warning++;
                }
            }
        }

        double complianceRate = totalCompleted > 0
                ? Math.round(((double) completedOnTime / totalCompleted) * 1000.0) / 10.0
                : 100.0;

        stats.setOverdueCount(overdue);
        stats.setSlaWarningCount(warning);
        stats.setSlaComplianceRate(complianceRate);
    }

    private long countByStatus(RequestStatus status) {
        return supportRequestRepository.countByStatus(status);
    }
}
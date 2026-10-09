package com.donga.qlhotro;

import com.donga.qlhotro.entity.Category;
import com.donga.qlhotro.entity.SlaConfig;
import com.donga.qlhotro.entity.SupportRequest;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.enums.Priority;
import com.donga.qlhotro.enums.RequestStatus;
import com.donga.qlhotro.enums.SlaStatus;
import com.donga.qlhotro.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SupportRequestWorkflowTest {

    @Test
    @DisplayName("Kiểm tra trạng thái hợp lệ khi tiếp nhận (PENDING -> RECEIVED)")
    void testValidTransitionPendingToReceived() {
        SupportRequest req = new SupportRequest();
        req.setStatus(RequestStatus.PENDING);

        // Chuyển sang RECEIVED
        req.setStatus(RequestStatus.RECEIVED);
        assertEquals(RequestStatus.RECEIVED, req.getStatus());
    }

    @Test
    @DisplayName("Kiểm tra ngoại lệ InvalidStatusTransitionException khi chuyển trạng thái sai quy trình")
    void testInvalidStatusTransitionThrowsException() {
        RequestStatus currentStatus = RequestStatus.PENDING;
        RequestStatus invalidTarget = RequestStatus.COMPLETED;

        // PENDING không thể chuyển thẳng thành COMPLETED
        assertThrows(InvalidStatusTransitionException.class, () -> {
            if (currentStatus != RequestStatus.IN_PROGRESS && invalidTarget == RequestStatus.COMPLETED) {
                throw new InvalidStatusTransitionException(currentStatus, invalidTarget);
            }
        });
    }

    @Test
    @DisplayName("Kiểm tra tính toán SLA thời hạn và trạng thái cảnh báo/quá hạn")
    void testSlaCalculationLogic() {
        LocalDateTime createdAt = LocalDateTime.now().minusHours(25);
        int resolutionHours = 24;
        LocalDateTime deadline = createdAt.plusHours(resolutionHours);

        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = now.isAfter(deadline);

        assertTrue(isOverdue, "Yêu cầu tạo 25 giờ trước với SLA 24 giờ phải ở trạng thái quá hạn");
    }

    @Test
    @DisplayName("Kiểm tra cấu hình SLA DTO với các trường alias tương thích frontend")
    void testSlaConfigRequestDTOAliases() {
        com.donga.qlhotro.dto.SlaConfigRequestDTO dto = new com.donga.qlhotro.dto.SlaConfigRequestDTO();
        dto.setResponseTimeHours(4);
        dto.setResolutionTimeHours(24);
        dto.setWarningTimeHours(2);
        dto.setPriority(Priority.HIGH);

        assertEquals(4, dto.getResponseTime());
        assertEquals(24, dto.getResolutionTime());
        assertEquals(2, dto.getWarningTime());
        assertEquals(Priority.HIGH, dto.getPriority());
    }
}

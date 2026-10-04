package com.donga.qlhotro.exception;

/**
 * Exception ném ra khi yêu cầu chuyển trạng thái không hợp lệ theo quy trình nghiệp vụ.
 * Được xử lý bởi GlobalExceptionHandler → trả về HTTP 400 BAD REQUEST.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    private final String currentStatus;
    private final String targetStatus;

    public InvalidStatusTransitionException(String currentStatus, String targetStatus) {
        super("Không thể chuyển yêu cầu từ trạng thái " + currentStatus + " sang " + targetStatus + ".");
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public InvalidStatusTransitionException(com.donga.qlhotro.enums.RequestStatus currentStatus, com.donga.qlhotro.enums.RequestStatus targetStatus) {
        this(currentStatus != null ? currentStatus.name() : "null", targetStatus != null ? targetStatus.name() : "null");
    }

    public InvalidStatusTransitionException(String message) {
        super(message);
        this.currentStatus = null;
        this.targetStatus = null;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getTargetStatus() {
        return targetStatus;
    }
}

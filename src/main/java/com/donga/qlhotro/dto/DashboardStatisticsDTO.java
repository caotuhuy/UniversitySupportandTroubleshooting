package com.donga.qlhotro.dto;

import java.util.HashMap;
import java.util.Map;

public class DashboardStatisticsDTO {

    private long totalRequests;
    private long pendingRequests;
    private long receivedRequests;
    private long assignedRequests;
    private long inProgressRequests;
    private long waitingInfoRequests;
    private long completedRequests;
    private long rejectedRequests;
    private long cancelledRequests;

    // Phân tích nâng cao
    private Map<String, Long> requestsByCategory = new HashMap<>();
    private Map<String, Long> requestsByPriority = new HashMap<>();
    private Map<String, Long> requestsByTechnician = new HashMap<>();

    // Thống kê SLA
    private long overdueCount;
    private long slaWarningCount;
    private double slaComplianceRate = 100.0;

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public long getReceivedRequests() {
        return receivedRequests;
    }

    public void setReceivedRequests(long receivedRequests) {
        this.receivedRequests = receivedRequests;
    }

    public long getAssignedRequests() {
        return assignedRequests;
    }

    public void setAssignedRequests(long assignedRequests) {
        this.assignedRequests = assignedRequests;
    }

    public long getInProgressRequests() {
        return inProgressRequests;
    }

    public void setInProgressRequests(long inProgressRequests) {
        this.inProgressRequests = inProgressRequests;
    }

    public long getWaitingInfoRequests() {
        return waitingInfoRequests;
    }

    public void setWaitingInfoRequests(long waitingInfoRequests) {
        this.waitingInfoRequests = waitingInfoRequests;
    }

    public long getCompletedRequests() {
        return completedRequests;
    }

    public void setCompletedRequests(long completedRequests) {
        this.completedRequests = completedRequests;
    }

    public long getRejectedRequests() {
        return rejectedRequests;
    }

    public void setRejectedRequests(long rejectedRequests) {
        this.rejectedRequests = rejectedRequests;
    }

    public long getCancelledRequests() {
        return cancelledRequests;
    }

    public void setCancelledRequests(long cancelledRequests) {
        this.cancelledRequests = cancelledRequests;
    }

    public Map<String, Long> getRequestsByCategory() {
        return requestsByCategory;
    }

    public void setRequestsByCategory(Map<String, Long> requestsByCategory) {
        this.requestsByCategory = requestsByCategory;
    }

    public Map<String, Long> getRequestsByPriority() {
        return requestsByPriority;
    }

    public void setRequestsByPriority(Map<String, Long> requestsByPriority) {
        this.requestsByPriority = requestsByPriority;
    }

    public Map<String, Long> getRequestsByTechnician() {
        return requestsByTechnician;
    }

    public void setRequestsByTechnician(Map<String, Long> requestsByTechnician) {
        this.requestsByTechnician = requestsByTechnician;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public long getSlaWarningCount() {
        return slaWarningCount;
    }

    public void setSlaWarningCount(long slaWarningCount) {
        this.slaWarningCount = slaWarningCount;
    }

    public double getSlaComplianceRate() {
        return slaComplianceRate;
    }

    public void setSlaComplianceRate(double slaComplianceRate) {
        this.slaComplianceRate = slaComplianceRate;
    }
}
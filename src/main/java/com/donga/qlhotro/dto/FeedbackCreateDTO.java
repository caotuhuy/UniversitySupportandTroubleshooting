package com.donga.qlhotro.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class FeedbackCreateDTO {

    @NotNull(message = "ID yêu cầu không được để trống")
    private Long requestId;

    @NotNull(message = "ID người đánh giá không được để trống")
    private Long userId;

    @NotNull(message = "Đánh giá không được để trống")
    @Min(value = 1, message = "Đánh giá tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa 5 sao")
    private Integer rating;

    private String comment;

    public FeedbackCreateDTO() {
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

package com.donga.qlhotro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.donga.qlhotro.entity.Feedback;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByRequestId(Long requestId);
    Optional<Feedback> findByRequestIdAndUserId(Long requestId, Long userId);
    List<Feedback> findByUserId(Long userId);
}

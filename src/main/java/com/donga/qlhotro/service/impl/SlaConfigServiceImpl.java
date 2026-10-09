package com.donga.qlhotro.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.donga.qlhotro.dto.SlaConfigRequestDTO;
import com.donga.qlhotro.dto.SlaConfigResponseDTO;
import com.donga.qlhotro.entity.SlaConfig;
import com.donga.qlhotro.enums.Priority;
import com.donga.qlhotro.enums.SlaStatus;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.SlaConfigRepository;
import com.donga.qlhotro.service.SlaConfigService;

@Service
public class SlaConfigServiceImpl implements SlaConfigService {

    private final SlaConfigRepository slaConfigRepository;

    public SlaConfigServiceImpl(SlaConfigRepository slaConfigRepository) {
        this.slaConfigRepository = slaConfigRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlaConfigResponseDTO> getAllConfigs() {
        return slaConfigRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SlaConfigResponseDTO getById(Long id) {
        SlaConfig config = slaConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cấu hình SLA", "id", id));
        return mapToDTO(config);
    }

    @Override
    @Transactional(readOnly = true)
    public SlaConfigResponseDTO getByPriority(Priority priority) {
        SlaConfig config = slaConfigRepository.findByPriority(priority)
                .orElseThrow(() -> new ResourceNotFoundException("Cấu hình SLA", "priority", priority));
        return mapToDTO(config);
    }

    @Override
    @Transactional
    public SlaConfigResponseDTO createConfig(SlaConfigRequestDTO request) {
        validateTimes(request);
        if (slaConfigRepository.findByPriority(request.getPriority()).isPresent()) {
            throw new IllegalArgumentException("Đã tồn tại cấu hình SLA cho mức độ ưu tiên này");
        }

        SlaConfig config = new SlaConfig();
        config.setPriority(request.getPriority());
        config.setResponseTime(request.getResponseTime());
        config.setResolutionTime(request.getResolutionTime());
        config.setWarningTime(request.getWarningTime());
        config.setStatus(request.getStatus() != null ? request.getStatus() : SlaStatus.ACTIVE);

        SlaConfig saved = slaConfigRepository.save(config);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public SlaConfigResponseDTO updateConfig(Long id, SlaConfigRequestDTO request) {
        SlaConfig config = slaConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cấu hình SLA", "id", id));

        validateTimes(request);
        slaConfigRepository.findByPriority(request.getPriority()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Đã tồn tại cấu hình SLA cho mức độ ưu tiên này");
            }
        });

        config.setPriority(request.getPriority());
        config.setResponseTime(request.getResponseTime());
        config.setResolutionTime(request.getResolutionTime());
        config.setWarningTime(request.getWarningTime());
        if (request.getStatus() != null) {
            config.setStatus(request.getStatus());
        }

        SlaConfig updated = slaConfigRepository.save(config);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteConfig(Long id) {
        if (!slaConfigRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cấu hình SLA", "id", id);
        }
        slaConfigRepository.deleteById(id);
    }

    private void validateTimes(SlaConfigRequestDTO request) {
        if (request.getWarningTime() > request.getResponseTime()
                || request.getResponseTime() > request.getResolutionTime()) {
            throw new IllegalArgumentException(
                    "Thời gian phải thỏa mãn warningTime <= responseTime <= resolutionTime");
        }
    }

    private SlaConfigResponseDTO mapToDTO(SlaConfig config) {
        SlaConfigResponseDTO dto = new SlaConfigResponseDTO();
        dto.setId(config.getId());
        dto.setPriority(config.getPriority());
        dto.setResponseTime(config.getResponseTime());
        dto.setResolutionTime(config.getResolutionTime());
        dto.setWarningTime(config.getWarningTime());
        dto.setStatus(config.getStatus());
        dto.setCreatedAt(config.getCreatedAt());
        dto.setUpdatedAt(config.getUpdatedAt());
        return dto;
    }
}

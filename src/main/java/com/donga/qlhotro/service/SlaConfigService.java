package com.donga.qlhotro.service;

import java.util.List;

import com.donga.qlhotro.dto.SlaConfigRequestDTO;
import com.donga.qlhotro.dto.SlaConfigResponseDTO;
import com.donga.qlhotro.enums.Priority;

public interface SlaConfigService {
    List<SlaConfigResponseDTO> getAllConfigs();
    SlaConfigResponseDTO getById(Long id);
    SlaConfigResponseDTO getByPriority(Priority priority);
    SlaConfigResponseDTO createConfig(SlaConfigRequestDTO request);
    SlaConfigResponseDTO updateConfig(Long id, SlaConfigRequestDTO request);
    void deleteConfig(Long id);
}

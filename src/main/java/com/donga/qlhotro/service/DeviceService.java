package com.donga.qlhotro.service;

import com.donga.qlhotro.dto.DeviceRequestDTO;
import com.donga.qlhotro.dto.DeviceResponseDTO;
import com.donga.qlhotro.enums.DeviceStatus;

import java.util.List;

public interface DeviceService {
    List<DeviceResponseDTO> getAllDevices();
    DeviceResponseDTO getDeviceById(Long id);
    DeviceResponseDTO createDevice(DeviceRequestDTO request);
    DeviceResponseDTO updateDevice(Long id, DeviceRequestDTO request);
    void deleteDevice(Long id);
    DeviceResponseDTO updateDeviceStatus(Long id, DeviceStatus status);
    DeviceResponseDTO assignDeviceToRoom(Long id, Long roomId);
}

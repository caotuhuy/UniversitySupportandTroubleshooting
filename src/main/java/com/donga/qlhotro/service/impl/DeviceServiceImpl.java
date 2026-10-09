package com.donga.qlhotro.service.impl;

import com.donga.qlhotro.dto.DeviceRequestDTO;
import com.donga.qlhotro.dto.DeviceResponseDTO;
import com.donga.qlhotro.dto.RoomResponseDTO;
import com.donga.qlhotro.entity.Device;
import com.donga.qlhotro.entity.Room;
import com.donga.qlhotro.enums.DeviceStatus;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.DeviceRepository;
import com.donga.qlhotro.repository.RoomRepository;
import com.donga.qlhotro.service.DeviceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository, RoomRepository roomRepository) {
        this.deviceRepository = deviceRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponseDTO> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponseDTO getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thiết bị", "id", id));
        return mapToResponseDTO(device);
    }

    @Override
    @Transactional
    public DeviceResponseDTO createDevice(DeviceRequestDTO request) {
        Device device = new Device();
        device.setDeviceCode(request.getDeviceCode());
        device.setDeviceName(request.getDeviceName());
        device.setDeviceType(request.getDeviceType());
        device.setDescription(request.getDescription());
        device.setStatus(request.getStatus() != null ? request.getStatus() : DeviceStatus.GOOD);
        device.setPurchaseDate(request.getPurchaseDate());

        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", request.getRoomId()));
            device.setRoom(room);
        }

        Device saved = deviceRepository.save(device);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public DeviceResponseDTO updateDevice(Long id, DeviceRequestDTO request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thiết bị", "id", id));

        device.setDeviceCode(request.getDeviceCode());
        device.setDeviceName(request.getDeviceName());
        device.setDeviceType(request.getDeviceType());
        device.setDescription(request.getDescription());
        device.setPurchaseDate(request.getPurchaseDate());
        if (request.getStatus() != null) {
            device.setStatus(request.getStatus());
        }

        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", request.getRoomId()));
            device.setRoom(room);
        } else {
            device.setRoom(null);
        }

        Device updated = deviceRepository.save(device);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Thiết bị", "id", id);
        }
        deviceRepository.deleteById(id);
    }

    @Override
    @Transactional
    public DeviceResponseDTO updateDeviceStatus(Long id, DeviceStatus status) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thiết bị", "id", id));
        device.setStatus(status);
        return mapToResponseDTO(deviceRepository.save(device));
    }

    @Override
    @Transactional
    public DeviceResponseDTO assignDeviceToRoom(Long id, Long roomId) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thiết bị", "id", id));

        if (roomId != null) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", roomId));
            device.setRoom(room);
        } else {
            device.setRoom(null);
        }

        return mapToResponseDTO(deviceRepository.save(device));
    }

    private DeviceResponseDTO mapToResponseDTO(Device device) {
        DeviceResponseDTO dto = new DeviceResponseDTO();
        dto.setId(device.getId());
        dto.setDeviceCode(device.getDeviceCode());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceType(device.getDeviceType());
        dto.setDescription(device.getDescription());
        dto.setStatus(device.getStatus());
        dto.setPurchaseDate(device.getPurchaseDate());
        dto.setCreatedAt(device.getCreatedAt());
        dto.setUpdatedAt(device.getUpdatedAt());

        if (device.getRoom() != null) {
            Room room = device.getRoom();
            RoomResponseDTO roomDTO = new RoomResponseDTO();
            roomDTO.setId(room.getId());
            roomDTO.setRoomCode(room.getRoomCode());
            roomDTO.setRoomName(room.getRoomName());
            roomDTO.setBuilding(room.getBuilding());
            roomDTO.setFloor(room.getFloor());
            roomDTO.setDescription(room.getDescription());
            roomDTO.setStatus(room.getStatus());
            dto.setRoom(roomDTO);
        }

        return dto;
    }
}

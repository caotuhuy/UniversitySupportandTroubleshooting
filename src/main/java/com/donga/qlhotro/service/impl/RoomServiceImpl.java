package com.donga.qlhotro.service.impl;

import com.donga.qlhotro.dto.RoomRequestDTO;
import com.donga.qlhotro.dto.RoomResponseDTO;
import com.donga.qlhotro.entity.Room;
import com.donga.qlhotro.enums.RoomStatus;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.RoomRepository;
import com.donga.qlhotro.service.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", id));
        return mapToResponseDTO(room);
    }

    @Override
    @Transactional
    public RoomResponseDTO createRoom(RoomRequestDTO request) {
        Room room = new Room();
        room.setRoomCode(request.getRoomCode());
        room.setRoomName(request.getRoomName());
        room.setBuilding(request.getBuilding());
        room.setFloor(request.getFloor());
        room.setDescription(request.getDescription());
        room.setStatus(request.getStatus() != null ? request.getStatus() : RoomStatus.ACTIVE);

        Room saved = roomRepository.save(room);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public RoomResponseDTO updateRoom(Long id, RoomRequestDTO request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", id));

        room.setRoomCode(request.getRoomCode());
        room.setRoomName(request.getRoomName());
        room.setBuilding(request.getBuilding());
        room.setFloor(request.getFloor());
        room.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }

        Room updated = roomRepository.save(room);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Phòng", "id", id);
        }
        roomRepository.deleteById(id);
    }

    private RoomResponseDTO mapToResponseDTO(Room room) {
        RoomResponseDTO dto = new RoomResponseDTO();
        dto.setId(room.getId());
        dto.setRoomCode(room.getRoomCode());
        dto.setRoomName(room.getRoomName());
        dto.setBuilding(room.getBuilding());
        dto.setFloor(room.getFloor());
        dto.setDescription(room.getDescription());
        dto.setStatus(room.getStatus());
        return dto;
    }
}

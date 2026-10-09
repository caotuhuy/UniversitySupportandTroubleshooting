package com.donga.qlhotro.service;

import com.donga.qlhotro.dto.RoomRequestDTO;
import com.donga.qlhotro.dto.RoomResponseDTO;

import java.util.List;

public interface RoomService {
    List<RoomResponseDTO> getAllRooms();
    RoomResponseDTO getRoomById(Long id);
    RoomResponseDTO createRoom(RoomRequestDTO request);
    RoomResponseDTO updateRoom(Long id, RoomRequestDTO request);
    void deleteRoom(Long id);
}

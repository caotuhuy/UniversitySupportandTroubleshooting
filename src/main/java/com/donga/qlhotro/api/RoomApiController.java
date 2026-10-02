package com.donga.qlhotro.api;

import com.donga.qlhotro.dto.ApiResponse;
import com.donga.qlhotro.dto.RoomRequestDTO;
import com.donga.qlhotro.dto.RoomResponseDTO;
import com.donga.qlhotro.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomApiController {

    private final RoomService roomService;

    public RoomApiController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponseDTO>>> getAllRooms() {
        List<RoomResponseDTO> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phòng thành công", rooms));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> getRoomById(@PathVariable Long id) {
        RoomResponseDTO room = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin phòng thành công", room));
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> createRoom(@Valid @RequestBody RoomRequestDTO request) {
        RoomResponseDTO createdRoom = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo phòng mới thành công", createdRoom));
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> updateRoom(@PathVariable Long id,
                                                                   @Valid @RequestBody RoomRequestDTO request) {
        RoomResponseDTO updatedRoom = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin phòng thành công", updatedRoom));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa phòng thành công", null));
    }
}

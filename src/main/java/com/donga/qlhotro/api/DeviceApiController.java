package com.donga.qlhotro.api;

import com.donga.qlhotro.dto.ApiResponse;
import com.donga.qlhotro.dto.DeviceRequestDTO;
import com.donga.qlhotro.dto.DeviceResponseDTO;
import com.donga.qlhotro.enums.DeviceStatus;
import com.donga.qlhotro.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceApiController {

    private final DeviceService deviceService;

    public DeviceApiController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceResponseDTO>>> getAllDevices() {
        List<DeviceResponseDTO> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thiết bị thành công", devices));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceResponseDTO>> getDeviceById(@PathVariable Long id) {
        DeviceResponseDTO device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thiết bị thành công", device));
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeviceResponseDTO>> createDevice(@Valid @RequestBody DeviceRequestDTO request) {
        DeviceResponseDTO createdDevice = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo thiết bị mới thành công", createdDevice));
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeviceResponseDTO>> updateDevice(@PathVariable Long id,
                                                                        @Valid @RequestBody DeviceRequestDTO request) {
        DeviceResponseDTO updatedDevice = deviceService.updateDevice(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thiết bị thành công", updatedDevice));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa thiết bị thành công", null));
    }

    @PutMapping("/{id}/status")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeviceResponseDTO>> updateDeviceStatus(@PathVariable Long id,
                                                                               @RequestParam DeviceStatus status) {
        DeviceResponseDTO updatedDevice = deviceService.updateDeviceStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thiết bị thành công", updatedDevice));
    }

    @PutMapping("/{id}/room/{roomId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeviceResponseDTO>> assignDeviceToRoom(@PathVariable Long id,
                                                                             @PathVariable Long roomId) {
        DeviceResponseDTO updatedDevice = deviceService.assignDeviceToRoom(id, roomId);
        return ResponseEntity.ok(ApiResponse.success("Gán thiết bị vào phòng thành công", updatedDevice));
    }
}

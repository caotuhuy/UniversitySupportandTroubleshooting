package com.donga.qlhotro.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.donga.qlhotro.dto.ApiResponse;
import com.donga.qlhotro.dto.SlaConfigRequestDTO;
import com.donga.qlhotro.dto.SlaConfigResponseDTO;
import com.donga.qlhotro.service.SlaConfigService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sla")
public class SlaConfigApiController {

    private final SlaConfigService slaConfigService;

    public SlaConfigApiController(SlaConfigService slaConfigService) {
        this.slaConfigService = slaConfigService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SlaConfigResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cấu hình SLA thành công.",
                slaConfigService.getAllConfigs()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SlaConfigResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy cấu hình SLA thành công.", slaConfigService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SlaConfigResponseDTO>> create(
            @Valid @RequestBody SlaConfigRequestDTO request) {
        SlaConfigResponseDTO created = slaConfigService.createConfig(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo cấu hình SLA thành công.", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SlaConfigResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody SlaConfigRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật cấu hình SLA thành công.",
                slaConfigService.updateConfig(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        slaConfigService.deleteConfig(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa cấu hình SLA thành công.", null));
    }
}
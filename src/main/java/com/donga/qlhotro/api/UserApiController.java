package com.donga.qlhotro.api;

import com.donga.qlhotro.dto.ApiResponse;
import com.donga.qlhotro.dto.UserRequestDTO;
import com.donga.qlhotro.dto.UserResponseDTO;
import com.donga.qlhotro.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo người dùng mới thành công", createdUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(@PathVariable Long id,
                                                                   @Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin người dùng thành công", updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công", null));
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<UserResponseDTO>> lockUser(@PathVariable Long id) {
        UserResponseDTO lockedUser = userService.lockUser(id);
        return ResponseEntity.ok(ApiResponse.success("Khóa tài khoản thành công", lockedUser));
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<UserResponseDTO>> unlockUser(@PathVariable Long id) {
        UserResponseDTO unlockedUser = userService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success("Mở khóa tài khoản thành công", unlockedUser));
    }
}

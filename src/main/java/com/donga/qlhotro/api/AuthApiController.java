package com.donga.qlhotro.api;

import com.donga.qlhotro.dto.ApiResponse;
import com.donga.qlhotro.dto.ChangePasswordDTO;
import com.donga.qlhotro.dto.ProfileUpdateDTO;
import com.donga.qlhotro.dto.RoleResponseDTO;
import com.donga.qlhotro.dto.UserResponseDTO;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.repository.UserRepository;
import com.donga.qlhotro.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthApiController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * GET /api/auth/me
     * Lấy thông tin tài khoản đang đăng nhập.
     * Bắt buộc phải có phiên đăng nhập hợp lệ.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser() {
        Optional<User> currentUserOpt = SecurityUtils.getCurrentUser(userRepository);
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Người dùng chưa đăng nhập hoặc phiên làm việc đã hết hạn."));
        }

        User user = currentUserOpt.get();
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng hiện tại thành công.", mapToResponseDTO(user)));
    }

    /**
     * PUT /api/auth/profile
     * Cập nhật thông tin cá nhân của người dùng đang đăng nhập.
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        Optional<User> currentUserOpt = SecurityUtils.getCurrentUser(userRepository);
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Người dùng chưa đăng nhập."));
        }

        User user = currentUserOpt.get();

        // Kiểm tra email nếu thay đổi
        if (!user.getEmail().equalsIgnoreCase(dto.getEmail())) {
            Optional<User> existingWithEmail = userRepository.findByEmail(dto.getEmail());
            if (existingWithEmail.isPresent() && !existingWithEmail.get().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Email đã được sử dụng bởi tài khoản khác."));
            }
            user.setEmail(dto.getEmail());
        }

        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());

        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân thành công.", mapToResponseDTO(saved)));
    }

    /**
     * PUT /api/auth/change-password
     * Đổi mật khẩu cho người dùng đang đăng nhập.
     */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        Optional<User> currentUserOpt = SecurityUtils.getCurrentUser(userRepository);
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Người dùng chưa đăng nhập."));
        }

        User user = currentUserOpt.get();

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Mật khẩu hiện tại không chính xác."));
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Xác nhận mật khẩu mới không khớp."));
        }

        // Kiểm tra không trùng mật khẩu cũ
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Mật khẩu mới không được trùng với mật khẩu hiện tại."));
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công.", null));
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStudentCode(user.getStudentCode());
        dto.setEmployeeCode(user.getEmployeeCode());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(role -> new RoleResponseDTO(role.getId(), role.getName(), role.getDescription()))
                    .collect(Collectors.toSet()));
        }
        return dto;
    }
}

package com.donga.qlhotro.security;

import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.enums.RoleName;
import com.donga.qlhotro.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<Authentication> getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.of(auth);
    }

    public static Optional<String> getCurrentUsername() {
        return getAuthentication().map(auth -> {
            if (auth.getPrincipal() instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }
            return auth.getName();
        });
    }

    public static Optional<CustomUserDetails> getCurrentUserDetails() {
        return getAuthentication().flatMap(auth -> {
            if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                return Optional.of(userDetails);
            }
            return Optional.empty();
        });
    }

    public static Optional<User> getCurrentUser(UserRepository userRepository) {
        return getCurrentUsername().flatMap(userRepository::findByUsername);
    }

    public static User getCurrentUserOrThrow(UserRepository userRepository) {
        return getCurrentUser(userRepository)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Người dùng chưa đăng nhập hoặc phiên làm việc đã hết hạn."));
    }

    public static boolean hasRole(RoleName roleName) {
        return hasRole(roleName.name());
    }

    public static boolean hasRole(String roleName) {
        return getAuthentication().map(auth ->
                auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> a.equals(roleName) || a.equals("ROLE_" + roleName))
        ).orElse(false);
    }

    public static boolean isAdmin() {
        return hasRole(RoleName.ROLE_ADMIN);
    }

    public static boolean isTechnician() {
        return hasRole(RoleName.ROLE_TECHNICIAN);
    }

    public static boolean isStudent() {
        return hasRole(RoleName.ROLE_STUDENT);
    }
}

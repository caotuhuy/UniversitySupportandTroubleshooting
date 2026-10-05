package com.donga.qlhotro.security;

import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.enums.UserStatus;
import com.donga.qlhotro.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với username: " + username));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new LockedException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new DisabledException("Tài khoản chưa được kích hoạt hoặc đã ngừng hoạt động.");
        }

        return new CustomUserDetails(user);
    }
}

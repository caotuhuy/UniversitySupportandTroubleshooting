package com.donga.qlhotro.config;

import com.donga.qlhotro.entity.Role;
import com.donga.qlhotro.entity.User;
import com.donga.qlhotro.enums.RoleName;
import com.donga.qlhotro.enums.UserStatus;
import com.donga.qlhotro.repository.RoleRepository;
import com.donga.qlhotro.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        upgradeExistingPasswordsToBcrypt();
        ensureTestAccounts();
    }

    private void initRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role(roleName, "Vai trò " + roleName.name());
                roleRepository.save(role);
                log.info("Khởi tạo vai trò mới: {}", roleName);
            }
        }
    }

    private void upgradeExistingPasswordsToBcrypt() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            String pwd = user.getPassword();
            if (pwd != null && !isBcrypt(pwd)) {
                user.setPassword(passwordEncoder.encode(pwd));
                userRepository.save(user);
                log.info("Đã mã hóa BCrypt cho tài khoản: {}", user.getUsername());
            }
        }
    }

    private void ensureTestAccounts() {
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElse(null);
        Role techRole = roleRepository.findByName(RoleName.ROLE_TECHNICIAN).orElse(null);
        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT).orElse(null);

        // 1. Admin account
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Quản trị viên Hệ thống");
            admin.setEmail("admin@donga.edu.vn");
            admin.setPhone("0901234567");
            admin.setEmployeeCode("ADM001");
            admin.setStatus(UserStatus.ACTIVE);
            if (adminRole != null) {
                admin.setRoles(new HashSet<>(Set.of(adminRole)));
            }
            userRepository.save(admin);
            log.info("Tạo mới tài khoản test Admin: admin / admin123");
        }

        // 2. Technician account
        if (userRepository.findByUsername("kt001").isEmpty()) {
            User tech = new User();
            tech.setUsername("kt001");
            tech.setPassword(passwordEncoder.encode("tech123"));
            tech.setFullName("Kỹ thuật viên Nguyễn Văn Kỹ");
            tech.setEmail("kt001@donga.edu.vn");
            tech.setPhone("0912345678");
            tech.setEmployeeCode("TECH001");
            tech.setStatus(UserStatus.ACTIVE);
            if (techRole != null) {
                tech.setRoles(new HashSet<>(Set.of(techRole)));
            }
            userRepository.save(tech);
            log.info("Tạo mới tài khoản test Technician: kt001 / tech123");
        }

        // 3. Student account
        if (userRepository.findByUsername("sv001").isEmpty()) {
            User student = new User();
            student.setUsername("sv001");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setFullName("Sinh viên Trần Văn Nam");
            student.setEmail("sv001@donga.edu.vn");
            student.setPhone("0923456789");
            student.setStudentCode("SV20240001");
            student.setStatus(UserStatus.ACTIVE);
            if (studentRole != null) {
                student.setRoles(new HashSet<>(Set.of(studentRole)));
            }
            userRepository.save(student);
            log.info("Tạo mới tài khoản test Student: sv001 / student123");
        }
    }

    private boolean isBcrypt(String password) {
        return password != null && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }
}

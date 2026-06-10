package com.xiangmuguanli.config;

import com.xiangmuguanli.entity.User;
import com.xiangmuguanli.enums.UserStatus;
import com.xiangmuguanli.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Initialize or reset admin user
        userRepository.findByUsername("admin").ifPresentOrElse(
            existing -> {
                existing.setPassword(passwordEncoder.encode("admin123"));
                userRepository.save(existing);
                log.info("Admin user password reset: admin / admin123");
            },
            () -> {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setName("管理员");
                admin.setEmail("admin@example.com");
                admin.setStatus(UserStatus.ACTIVE);
                admin.setAdmin(true);
                userRepository.save(admin);
                log.info("Default admin user created: admin / admin123");
            }
        );

        // Initialize or reset normal user
        userRepository.findByUsername("user").ifPresentOrElse(
            existing -> {
                existing.setPassword(passwordEncoder.encode("admin123"));
                userRepository.save(existing);
                log.info("User password reset: user / admin123");
            },
            () -> {
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("admin123"));
                user.setName("普通用户");
                user.setEmail("user@example.com");
                user.setStatus(UserStatus.ACTIVE);
                user.setAdmin(false);
                userRepository.save(user);
                log.info("Default user created: user / admin123");
            }
        );
    }
}

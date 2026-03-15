package ru.rbpo.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.rbpo.backend.model.Role;
import ru.rbpo.backend.model.User;
import ru.rbpo.backend.repository.UserRepository;

/**
 * Создаёт тестовых пользователей при первом запуске (как в music-streaming).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        userRepository.findByUsername("admin").ifPresentOrElse(
                admin -> {
                    admin.setPassword(passwordEncoder.encode("Admin123!@#"));
                    admin.setRole(Role.ADMIN);
                    admin.setEmail("admin@example.com");
                    admin.setFirstName("Admin");
                    admin.setLastName("User");
                    userRepository.save(admin);
                },
                () -> userRepository.save(new User(
                        "Admin", "User", "admin@example.com",
                        "admin", passwordEncoder.encode("Admin123!@#"), Role.ADMIN))
        );

        userRepository.findByUsername("testuser").ifPresentOrElse(
                testUser -> {
                    testUser.setPassword(passwordEncoder.encode("Test123!@#"));
                    testUser.setRole(Role.USER);
                    testUser.setEmail("user@example.com");
                    testUser.setFirstName("Test");
                    testUser.setLastName("User");
                    userRepository.save(testUser);
                },
                () -> userRepository.save(new User(
                        "Test", "User", "user@example.com",
                        "testuser", passwordEncoder.encode("Test123!@#"), Role.USER))
        );
    }
}

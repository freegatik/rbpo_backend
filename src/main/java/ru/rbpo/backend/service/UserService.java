package ru.rbpo.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rbpo.backend.dto.RegistrationRequest;
import ru.rbpo.backend.model.Role;
import ru.rbpo.backend.model.User;
import ru.rbpo.backend.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Пользователь с именем " + request.getUsername() + " уже существует");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Пользователь с email " + request.getEmail() + " уже существует");
        }
        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER
        );
        return userRepository.save(user);
    }
}

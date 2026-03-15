package ru.rbpo.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.rbpo.backend.dto.*;
import ru.rbpo.backend.model.User;
import ru.rbpo.backend.model.UserSession;
import ru.rbpo.backend.security.CurrentUserProvider;
import ru.rbpo.backend.security.SecurityUser;
import ru.rbpo.backend.service.TokenService;
import ru.rbpo.backend.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final CurrentUserProvider currentUserProvider;

    public AuthController(UserService userService, TokenService tokenService,
                          AuthenticationManager authenticationManager, CurrentUserProvider currentUserProvider) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        User user = userService.registerUser(request);
        RegistrationResponse response = new RegistrationResponse(
                user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User user = securityUser.getUser();

        String deviceId = request.getUsername() + "_" + System.currentTimeMillis();
        UserSession session = tokenService.createSession(user, deviceId);
        return ResponseEntity.ok(new TokenResponse(session.getAccessToken(), session.getRefreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        UserSession newSession = tokenService.refreshTokens(request.getRefreshToken());
        return ResponseEntity.ok(new TokenResponse(newSession.getAccessToken(), newSession.getRefreshToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser == null) {
            throw new org.springframework.security.access.AccessDeniedException("Пользователь не аутентифицирован");
        }
        CurrentUserResponse response = new CurrentUserResponse(
                currentUser.getId(), currentUser.getUsername(), currentUser.getEmail(),
                currentUser.getFirstName(), currentUser.getLastName(), currentUser.getRole());
        return ResponseEntity.ok(response);
    }
}

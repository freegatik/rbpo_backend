package ru.rbpo.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.rbpo.backend.exception.TokenException;
import ru.rbpo.backend.model.SessionStatus;
import ru.rbpo.backend.model.User;
import ru.rbpo.backend.model.UserSession;
import ru.rbpo.backend.repository.UserRepository;
import ru.rbpo.backend.repository.UserSessionRepository;
import ru.rbpo.backend.security.JwtTokenProvider;

import java.time.Instant;
import java.util.UUID;

/** Выдача и проверка JWT (access/refresh), хранение сессий в user_sessions. */
@Service
public class TokenService {

    private final UserSessionRepository sessionRepository;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;

    public TokenService(UserSessionRepository sessionRepository, JwtTokenProvider tokenProvider,
                        UserRepository userRepository, org.springframework.transaction.PlatformTransactionManager transactionManager) {
        this.sessionRepository = sessionRepository;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        org.springframework.transaction.support.DefaultTransactionDefinition def = new org.springframework.transaction.support.DefaultTransactionDefinition();
        def.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate = new TransactionTemplate(transactionManager, def);
    }

    @Transactional
    public UserSession createSession(User user, String deviceId) {
        UUID sessionId = UUID.randomUUID();
        String username = user.getUsername();
        String email = user.getEmail();
        String role = user.getRole().name();

        String accessToken = tokenProvider.generateAccessToken(username, email, role);
        String refreshToken = tokenProvider.generateRefreshToken(username, email, sessionId.toString());

        Instant now = Instant.now();
        Instant accessExpiry = now.plusMillis(tokenProvider.getAccessTokenExpiration());
        Instant refreshExpiry = now.plusMillis(tokenProvider.getRefreshTokenExpiration());

        UserSession session = new UserSession(
                user.getEmail(), deviceId, accessToken, refreshToken,
                accessExpiry, refreshExpiry, SessionStatus.ACTIVE
        );
        return sessionRepository.save(session);
    }

    @Transactional
    public UserSession refreshTokens(String refreshToken) {
        if (!tokenProvider.validateRefreshToken(refreshToken)) {
            throw new TokenException("Невалидный refresh токен");
        }
        UserSession session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new TokenException("Сессия не найдена"));

        if (session.getStatus() == SessionStatus.USED) {
            revokeSessionInNewTransaction(session.getId());
            throw new TokenException("Обнаружена попытка повторного использования refresh токена");
        }
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new TokenException("Сессия неактивна");
        }
        if (session.getRefreshTokenExpiry().isBefore(Instant.now())) {
            revokeSessionInNewTransaction(session.getId());
            throw new TokenException("Refresh токен истек");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        String username = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new TokenException("Пользователь не найден"));
        String role = user.getRole().name();

        session.setStatus(SessionStatus.USED);
        sessionRepository.save(session);

        UUID newSessionId = UUID.randomUUID();
        String newAccessToken = tokenProvider.generateAccessToken(username, email, role);
        String newRefreshToken = tokenProvider.generateRefreshToken(username, email, newSessionId.toString());
        Instant now = Instant.now();
        Instant accessExpiry = now.plusMillis(tokenProvider.getAccessTokenExpiration());
        Instant refreshExpiry = now.plusMillis(tokenProvider.getRefreshTokenExpiration());

        UserSession newSession = new UserSession(
                email, session.getDeviceId(), newAccessToken, newRefreshToken,
                accessExpiry, refreshExpiry, SessionStatus.ACTIVE
        );
        return sessionRepository.save(newSession);
    }

    public void revokeSessionInNewTransaction(UUID sessionId) {
        transactionTemplate.executeWithoutResult(status -> {
            UserSession session = sessionRepository.findById(sessionId).orElse(null);
            if (session != null) {
                session.setStatus(SessionStatus.REVOKED);
                sessionRepository.saveAndFlush(session);
            }
        });
    }
}

package ru.rbpo.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.rbpo.backend.model.Role;
import ru.rbpo.backend.model.User;

/** Текущий пользователь из SecurityContext (после JwtAuthenticationFilter). */
@Component
public class CurrentUserProvider {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof SecurityUser)) {
            return null;
        }
        return ((SecurityUser) principal).getUser();
    }

    public boolean isAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }
}

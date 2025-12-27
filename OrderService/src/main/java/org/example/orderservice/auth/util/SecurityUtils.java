package org.example.orderservice.auth.util;

import org.example.orderservice.auth.model.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) authentication).getUserId();
        }
        throw new SecurityException("User ID not found in security context");
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public static boolean isService() {
        return hasRole("ROLE_ADMIN");
    }

}
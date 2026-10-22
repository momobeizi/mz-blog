package com.mz.blog.common.util;

import com.mz.blog.security.AdminUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AdminUserDetails currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminUserDetails details) {
            return details;
        }
        return null;
    }

    public static Long currentUserId() {
        AdminUserDetails user = currentUser();
        return user == null ? null : user.getId();
    }

    public static String currentUsername() {
        AdminUserDetails user = currentUser();
        return user == null ? "system" : user.getUsername();
    }
}

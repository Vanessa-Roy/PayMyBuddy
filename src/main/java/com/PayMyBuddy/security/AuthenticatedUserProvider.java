package com.PayMyBuddy.security;

import com.PayMyBuddy.model.User;
import com.PayMyBuddy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * The initialization of Authenticated user provider.
 */
@Service
public class AuthenticatedUserProvider {

    @Autowired
    UserService userService;

    /**
     * Gets authenticated user.
     *
     * @return the authenticated user
     */
    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.loadUserByUsername(auth.getName());
    }
}

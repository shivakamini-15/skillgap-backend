package com.skillgap.security;

import com.skillgap.entity.User;
import com.skillgap.exception.ResourceNotFoundException;
import com.skillgap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Resolves the logged-in User entity from the JWT-authenticated security context. */
@Component
@RequiredArgsConstructor
public class CurrentUser {
    private final UserRepository userRepository;

    public User get() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

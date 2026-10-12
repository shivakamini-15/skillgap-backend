package com.skillgap.service;

import com.skillgap.dto.Dtos.*;
import com.skillgap.entity.Role;
import com.skillgap.entity.User;
import com.skillgap.exception.BadRequestException;
import com.skillgap.repository.UserRepository;
import com.skillgap.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /** Self-registration always creates a STUDENT; admins and mentors are created by the seeder / DB. */
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }
        User u = new User();
        u.setFullName(req.fullName().trim());
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setRole(Role.STUDENT);
        u.setEducation(req.education());
        u.setCareerInterests(req.careerInterests());
        u = userRepository.save(u);
        return toResponse(u);
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        User u = userRepository.findByEmail(email).orElseThrow();
        return toResponse(u);
    }

    private AuthResponse toResponse(User u) {
        String token = jwtUtil.generate(u.getEmail(), u.getRole().name());
        return new AuthResponse(token, u.getId(), u.getFullName(), u.getEmail(), u.getRole().name());
    }
}

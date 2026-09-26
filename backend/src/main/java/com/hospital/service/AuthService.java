package com.hospital.service;

import com.hospital.dto.AuthResponse;
import com.hospital.dto.LoginRequest;
import com.hospital.dto.UserSummaryDto;
import com.hospital.entity.User;
import com.hospital.exception.ForbiddenException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.UnauthorizedException;
import com.hospital.repository.UserRepository;
import com.hospital.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuditLogService auditLogService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    auditLogService.logAction(null, "LOGIN_FAILURE", "AUTHENTICATION", null, "Failed login attempt for username: " + request.getUsername());
                    return new UnauthorizedException("Invalid username or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            auditLogService.logAction(user.getId(), "LOGIN_FAILURE", "AUTHENTICATION", user.getId().toString(), "Incorrect password attempt for user: " + user.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            auditLogService.logAction(user.getId(), "LOGIN_FAILURE", "AUTHENTICATION", user.getId().toString(), "Login attempted on inactive account: " + user.getUsername());
            throw new ForbiddenException("Account is inactive");
        }

        String roleName = user.getRole().getName().name();
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), roleName);

        auditLogService.logAction(user.getId(), "LOGIN_SUCCESS", "AUTHENTICATION", user.getId().toString(), "User logged in successfully");

        UserSummaryDto userSummary = UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(roleName)
                .isActive(user.getIsActive())
                .build();

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(userSummary)
                .build();
    }

    public UserSummaryDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName().name())
                .isActive(user.getIsActive())
                .build();
    }

    public void logout(Long userId, String username) {
        auditLogService.logAction(userId, "LOGOUT", "AUTHENTICATION", String.valueOf(userId), "User logged out");
    }
}

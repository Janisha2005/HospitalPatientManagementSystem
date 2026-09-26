package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.RoleRepository;
import com.hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public Page<UserResponseDto> getAllUsers(String roleFilter, Pageable pageable) {
        if (roleFilter != null && !roleFilter.trim().isEmpty()) {
            try {
                Role.RoleName roleName = Role.RoleName.valueOf(roleFilter.toUpperCase());
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleFilter));
                return userRepository.findByRole(role, pageable).map(this::mapToDto);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role filter: " + roleFilter);
            }
        }
        return userRepository.findAll(pageable).map(this::mapToDto);
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDto(user);
    }

    @Transactional
    public UserResponseDto createUser(CreateUserRequest request, Long currentUserId) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        Role.RoleName roleName;
        try {
            roleName = Role.RoleName.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.getRole());
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(role)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        auditLogService.logAction(currentUserId, "CREATE_USER", "USER", savedUser.getId().toString(),
                "Created user: " + savedUser.getUsername() + " with role: " + roleName);

        return mapToDto(savedUser);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UpdateUserRequest request, Long currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }

        Role.RoleName roleName;
        try {
            roleName = Role.RoleName.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.getRole());
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(role);
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        User updatedUser = userRepository.save(user);

        auditLogService.logAction(currentUserId, "UPDATE_USER", "USER", updatedUser.getId().toString(),
                "Updated user: " + updatedUser.getUsername());

        return mapToDto(updatedUser);
    }

    @Transactional
    public UserResponseDto updateUserStatus(Long id, Boolean isActive, Long currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setIsActive(isActive);
        User updatedUser = userRepository.save(user);

        auditLogService.logAction(currentUserId, "UPDATE_USER_STATUS", "USER", updatedUser.getId().toString(),
                "Set user active status to: " + isActive + " for user: " + updatedUser.getUsername());

        return mapToDto(updatedUser);
    }

    @Transactional
    public void resetPassword(Long id, PasswordResetRequest request, Long currentUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLogService.logAction(currentUserId, "RESET_PASSWORD", "USER", user.getId().toString(),
                "Reset password for user: " + user.getUsername());
    }

    private UserResponseDto mapToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole().getName().name())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

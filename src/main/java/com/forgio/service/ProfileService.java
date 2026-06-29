package com.forgio.service;

import com.forgio.dto.request.ChangePasswordRequest;
import com.forgio.dto.request.UpdateProfileRequest;
import com.forgio.dto.response.ProfileResponse;
import com.forgio.entity.Department;
import com.forgio.entity.Factory;
import com.forgio.entity.User;
import com.forgio.exception.BadRequestException;
import com.forgio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile() {
        return toResponse(currentUser());
    }

    @Transactional
    public ProfileResponse updateMyProfile(UpdateProfileRequest req) {
        User user = currentUser();

        if (!user.getPhone().equals(req.phone())
                && userRepository.existsByPhone(req.phone())) {
            throw new BadRequestException("That phone number is already in use");
        }

        user.setName(req.name());
        user.setPhone(req.phone());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        User user = currentUser();

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    private ProfileResponse toResponse(User u) {
        Factory f = u.getFactory();
        Department d = u.getDepartment();
        return new ProfileResponse(
                u.getUserId(),
                u.getName(),
                u.getPhone(),
                u.getRole(),
                f != null ? f.getFactoryId() : null,
                f != null ? f.getName()      : null,
                d != null ? d.getDeptId()    : null,
                d != null ? d.getName()      : null);
    }
}
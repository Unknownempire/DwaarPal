package com.pranav.society_vehicle_manager.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.pranav.society_vehicle_manager.dto.UserRegistrationRequest;
import com.pranav.society_vehicle_manager.dto.UserResponse;
import com.pranav.society_vehicle_manager.entity.User;
import com.pranav.society_vehicle_manager.enums.AccountStatus;
import com.pranav.society_vehicle_manager.enums.Role;
import com.pranav.society_vehicle_manager.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
//    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse registerResident(UserRegistrationRequest request) {
        String phone = request.getPhone().trim();
        String email = request.getEmail() == null || request.getEmail().isBlank()
                ? null : request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByPhone(phone) || (email != null && userRepository.existsByEmailIgnoreCase(email))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number or email is already registered");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setPhone(phone);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.RESIDENT);
        user.setStatus(AccountStatus.ACTIVE);
        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        return response;
    }
}

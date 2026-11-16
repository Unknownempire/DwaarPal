package com.pranav.society_vehicle_manager.service;

import com.pranav.society_vehicle_manager.dto.LoginRequest;
import com.pranav.society_vehicle_manager.dto.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse authenticate(LoginRequest request) {
        UsernamePasswordAuthenticationToken credentials =
                new UsernamePasswordAuthenticationToken(
                        request.getPhone(),
                        request.getPassword());

        Authentication authentication = authenticationManager.authenticate(credentials);

        String token = jwtService.generateToken(authentication);

        return new LoginResponse(
                token,
                "Bearer",
                JwtService.ACCESS_TOKEN_TTL_SECONDS
        );

    }

}

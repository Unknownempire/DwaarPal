package com.pranav.society_vehicle_manager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pranav.society_vehicle_manager.dto.GateRequest;
import com.pranav.society_vehicle_manager.dto.GateResponse;
import com.pranav.society_vehicle_manager.service.GateService;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api")
public class GateController {
    private final GateService gateService;

    public GateController(GateService gateService) {
        this.gateService = gateService;
    }

    @PostMapping("/societies/{societyId}/gates")
    public ResponseEntity<GateResponse> createGate(
            @PathVariable long societyId,
            @Valid @RequestBody GateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        long authenticatedAdminId = Long.parseLong(jwt.getSubject());
        GateResponse response = gateService.createGate(authenticatedAdminId, societyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/gates")
    public ResponseEntity<List<GateResponse>> getSocietyGates(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam long societyId) {
        long authenticatedUserId = Long.parseLong(jwt.getSubject());
        List<GateResponse> response = gateService.getGates(authenticatedUserId, societyId);
        return ResponseEntity.ok(response);
    }

}

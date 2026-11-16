package com.pranav.society_vehicle_manager.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pranav.society_vehicle_manager.dto.EntryExitResponse;
import com.pranav.society_vehicle_manager.dto.VehicleRequest;
import com.pranav.society_vehicle_manager.dto.VehicleResponse;
import com.pranav.society_vehicle_manager.enums.EntryExitType;
import com.pranav.society_vehicle_manager.enums.Status;
import com.pranav.society_vehicle_manager.service.VehicleService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequestMapping("/api")
@RestController
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/vehicles/{registrationNumber}")
    public ResponseEntity<VehicleResponse> getVehicle(@PathVariable String registrationNumber) {
        VehicleResponse response = vehicleService.findByRegistrationNumber(registrationNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/vehicles")
    public ResponseEntity<VehicleResponse> createVehicle(
            @Valid @RequestBody VehicleRequest request,
            @RequestParam long membershipId,
            @AuthenticationPrincipal Jwt jwt) {

        long authenticatedUserId = Long.parseLong(jwt.getSubject());
        VehicleResponse response =
                vehicleService.createVehicle(request, membershipId, authenticatedUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/vehicles/{registrationNumber}/status")
    public ResponseEntity<VehicleResponse> updateVehicleStatus(
            @PathVariable String registrationNumber,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam long societyId,
            @RequestParam Status updateStatus) {
        long authenticatedUserId = Long.parseLong(jwt.getSubject());

        VehicleResponse response = vehicleService.updateVehicleStatus(
                registrationNumber,
                authenticatedUserId,
                societyId,
                updateStatus);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicles/pending")
    public ResponseEntity<List<VehicleResponse>> getPendingVehicles(
            @AuthenticationPrincipal Jwt jwt, @RequestParam long societyId) {

        long authenticatedUserId = Long.parseLong(jwt.getSubject());
        List<VehicleResponse> response = vehicleService.getPendingVehicle(authenticatedUserId, societyId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/vehicles/{registrationNumber}/entry-exit")
    public ResponseEntity<EntryExitResponse> processVehicleEntryExit(
            @PathVariable String registrationNumber,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam EntryExitType entryExitType,
            @RequestParam long gateId) {

        long authenticatedUserId = Long.parseLong(jwt.getSubject());
        EntryExitResponse response = vehicleService.processVehicleEntryExit(registrationNumber, authenticatedUserId, entryExitType, gateId);
        // return ResponseEntity.ok(response);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(response);
    }

    @GetMapping("/vehicles/my-vehicles")
    public ResponseEntity<List<VehicleResponse>> getUsersVehicles(@AuthenticationPrincipal Jwt jwt) {
        long authenticatedUserId = Long.parseLong(jwt.getSubject());
        List<VehicleResponse> response = vehicleService.myVehicles(authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    
}

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
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request,
            @RequestParam Long userId) {
        VehicleResponse response = vehicleService.createVehicle(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PatchMapping("/vehicles/{registrationNumber}/status")
    public ResponseEntity<VehicleResponse> updateVehicleStatus(
            @PathVariable String registrationNumber,
            @RequestParam Long userId,
            @RequestParam Status updateStatus) {

        VehicleResponse response = vehicleService.updateVehicleStatus(
                registrationNumber,
                userId,
                updateStatus);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicles/pending")
    public ResponseEntity<List<VehicleResponse>> getPendingVehicles(
            @RequestParam Long userId) {

        List<VehicleResponse> response = vehicleService.getPendingVehicle(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/vehicles/{registrationNumber}/entry-exit")
    public ResponseEntity<EntryExitResponse> processVehicleEntryExit(
            @PathVariable String registrationNumber,
            @RequestParam long userId,
            @RequestParam EntryExitType entryExitType,
            @RequestParam long gateId) {

        EntryExitResponse response = vehicleService.processVehicleEntryExit(registrationNumber, userId, entryExitType, gateId);
        // return ResponseEntity.ok(response);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(response);
    }

    @GetMapping("/vehicles/my-vehicles")
    public ResponseEntity<List<VehicleResponse>> getUsersVehicles(@RequestParam long userId) {
        List<VehicleResponse> response = vehicleService.myVehicles(userId);
        return ResponseEntity.ok(response);
    }

    
}
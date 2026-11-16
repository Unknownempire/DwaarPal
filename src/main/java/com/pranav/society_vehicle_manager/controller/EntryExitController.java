package com.pranav.society_vehicle_manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranav.society_vehicle_manager.dto.EntryExitResponse;
import com.pranav.society_vehicle_manager.service.EntryExitService;

@RestController 
@RequestMapping("/api/entry-exit")
public class EntryExitController {
    private final EntryExitService entryExitService;

    public EntryExitController(EntryExitService entryExitService) {
        this.entryExitService = entryExitService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<EntryExitResponse>> getEntryExitHistory(@AuthenticationPrincipal Jwt jwt) {
        long authenticatedUserId = Long.parseLong(jwt.getSubject());
        List<EntryExitResponse> response = entryExitService.getSocietyEntryExitHistory(authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vehicles/{registrationNumber}/history")
    public ResponseEntity<List<EntryExitResponse>> getVehicleEntryExitHistory(
            @PathVariable String registrationNumber,
            @AuthenticationPrincipal Jwt jwt) {
        long authenticatedUserId = Long.parseLong(jwt.getSubject());
        List<EntryExitResponse> response = entryExitService.getVehicleEntryExitHistory(
                authenticatedUserId, registrationNumber);
        return ResponseEntity.ok(response);
    }

}

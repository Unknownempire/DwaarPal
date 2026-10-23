package com.pranav.society_vehicle_manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pranav.society_vehicle_manager.dto.GateResponse;
import com.pranav.society_vehicle_manager.service.GateService;
import org.springframework.web.bind.annotation.GetMapping;


@RestController 
@RequestMapping("/api")
public class GateController {
    private final GateService gateService;

    public GateController(GateService gateService) {
        this.gateService = gateService;
    }

    @GetMapping("/gates")
    public ResponseEntity<List<GateResponse>> getSocietyGates(@RequestParam long userId) {
        List<GateResponse> response = gateService.getGates(userId);
        return ResponseEntity.ok(response);
    }

}

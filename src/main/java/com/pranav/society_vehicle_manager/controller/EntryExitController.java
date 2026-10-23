package com.pranav.society_vehicle_manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<List<EntryExitResponse>> getEntryExitHistory(@RequestParam long userId) {
        List<EntryExitResponse> response = entryExitService.getSocietyEntryExitHistory(userId);
        return ResponseEntity.ok(response);
    }

}

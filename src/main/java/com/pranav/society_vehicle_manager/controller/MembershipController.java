package com.pranav.society_vehicle_manager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pranav.society_vehicle_manager.dto.MembershipApplicationRequest;
import com.pranav.society_vehicle_manager.dto.MembershipResponse;
import com.pranav.society_vehicle_manager.enums.MembershipStatus;
import com.pranav.society_vehicle_manager.service.MembershipService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {
    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    public ResponseEntity<MembershipResponse> apply(@RequestParam long userId,
            @Valid @RequestBody MembershipApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.apply(userId, request));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MembershipResponse>> mine(@RequestParam long userId) {
        return ResponseEntity.ok(membershipService.myMemberships(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<MembershipResponse>> pending(@RequestParam long adminUserId,
            @RequestParam long societyId) {
        return ResponseEntity.ok(membershipService.pendingForSociety(adminUserId, societyId));
    }

    @PatchMapping("/{membershipId}/decision")
    public ResponseEntity<MembershipResponse> decide(@PathVariable long membershipId,
            @RequestParam long adminUserId, @RequestParam MembershipStatus decision) {
        return ResponseEntity.ok(membershipService.decide(membershipId, adminUserId, decision));
    }

    @PatchMapping("/{membershipId}/leave")
    public ResponseEntity<MembershipResponse> leave(@PathVariable long membershipId, @RequestParam long userId) {
        return ResponseEntity.ok(membershipService.leave(membershipId, userId));
    }
}

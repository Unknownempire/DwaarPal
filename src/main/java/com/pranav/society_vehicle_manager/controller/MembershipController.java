package com.pranav.society_vehicle_manager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<MembershipResponse> apply(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MembershipApplicationRequest request) {
        long authenticatedUserId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.apply(authenticatedUserId, request));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MembershipResponse>> mine(@AuthenticationPrincipal Jwt jwt) {
        long authenticatedUserId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(membershipService.myMemberships(authenticatedUserId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<MembershipResponse>> pending(@AuthenticationPrincipal Jwt jwt,
            @RequestParam long societyId) {
        long authenticatedUserId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(membershipService.pendingForSociety(authenticatedUserId, societyId));
    }

    @PatchMapping("/{membershipId}/decision")
    public ResponseEntity<MembershipResponse> decide(@PathVariable long membershipId,
            @AuthenticationPrincipal Jwt jwt, @RequestParam MembershipStatus decision) {
        long authenticatedUserId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(membershipService.decide(membershipId, authenticatedUserId, decision));
    }

    @PatchMapping("/{membershipId}/leave")
    public ResponseEntity<MembershipResponse> leave(@PathVariable long membershipId,
            @AuthenticationPrincipal Jwt jwt) {
        long authenticatedUserId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(membershipService.leave(membershipId, authenticatedUserId));
    }
}

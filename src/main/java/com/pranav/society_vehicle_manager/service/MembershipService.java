package com.pranav.society_vehicle_manager.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pranav.society_vehicle_manager.dto.MembershipApplicationRequest;
import com.pranav.society_vehicle_manager.dto.MembershipResponse;
import com.pranav.society_vehicle_manager.entity.Flat;
import com.pranav.society_vehicle_manager.entity.Membership;
import com.pranav.society_vehicle_manager.entity.Society;
import com.pranav.society_vehicle_manager.entity.User;
import com.pranav.society_vehicle_manager.enums.MembershipStatus;
import com.pranav.society_vehicle_manager.enums.AccountStatus;
import com.pranav.society_vehicle_manager.enums.Role;
import com.pranav.society_vehicle_manager.exception.AccessDeniedException;
import com.pranav.society_vehicle_manager.exception.MembershipConflictException;
import com.pranav.society_vehicle_manager.exception.SocietyNotFoundException;
import com.pranav.society_vehicle_manager.exception.UserNotFoundException;
import com.pranav.society_vehicle_manager.mapper.MembershipMapper;
import com.pranav.society_vehicle_manager.repository.FlatRepository;
import com.pranav.society_vehicle_manager.repository.MembershipRepository;
import com.pranav.society_vehicle_manager.repository.SocietyRepository;
import com.pranav.society_vehicle_manager.repository.UserRepository;

@Service
public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final SocietyRepository societyRepository;
    private final FlatRepository flatRepository;
    private final MembershipMapper membershipMapper;

    public MembershipService(MembershipRepository membershipRepository, UserRepository userRepository,
            SocietyRepository societyRepository, FlatRepository flatRepository, MembershipMapper membershipMapper) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.societyRepository = societyRepository;
        this.flatRepository = flatRepository;
        this.membershipMapper = membershipMapper;
    }

    @Transactional
    public MembershipResponse apply(Long authenticatedUserId, MembershipApplicationRequest request) {
        User user = requireActiveResident(authenticatedUserId);
        Society society = societyRepository.findById(request.getSocietyId())
                .orElseThrow(() -> new SocietyNotFoundException("Society not found"));
        Flat flat = flatRepository.findById(request.getFlatId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flat not found"));
        if (!flat.getBuilding().getSociety().getId().equals(society.getId())) {
            throw new MembershipConflictException("The selected flat is not in the selected society");
        }
        if (membershipRepository.existsByUserAndSocietyAndStatus(user, society, MembershipStatus.ACTIVE)) {
            throw new MembershipConflictException("User already has an active membership in this society");
        }
        if (membershipRepository.existsByUserAndSocietyAndStatus(user, society, MembershipStatus.PENDING)) {
            throw new MembershipConflictException("User already has a pending membership in this society");
        }

        Membership membership = new Membership();
        membership.setUser(user);
        membership.setSociety(society);
        membership.setFlat(flat);
        membership.setStatus(MembershipStatus.PENDING);
        membership.setAppliedAt(LocalDateTime.now());
        return membershipMapper.toResponse(membershipRepository.save(membership));
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> myMemberships(long authenticatedUserId) {
        User user = requireActiveResident(authenticatedUserId);
        return membershipRepository.findByUserOrderByAppliedAtDesc(user).stream()
                .map(membershipMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> pendingForSociety(long authenticatedUserId, long societyId) {
        User admin = getUser(authenticatedUserId);
        Society society = societyRepository.findById(societyId)
                .orElseThrow(() -> new SocietyNotFoundException("Society not found"));
        requireAdminOfSociety(admin, society);
        return membershipRepository.findBySocietyAndStatusOrderByAppliedAtAsc(society, MembershipStatus.PENDING)
                .stream().map(membershipMapper::toResponse).toList();
    }

    @Transactional
    public MembershipResponse decide(long membershipId, long authenticatedUserId, MembershipStatus decision) {
        if (decision != MembershipStatus.ACTIVE && decision != MembershipStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be ACTIVE or REJECTED");
        }
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found"));
        User admin = getUser(authenticatedUserId);
        requireAdminOfSociety(admin, membership.getSociety());
        if (membership.getStatus() != MembershipStatus.PENDING) {
            throw new MembershipConflictException("Membership request has already been processed");
        }
        if (decision == MembershipStatus.ACTIVE) {
            requireActiveResident(membership.getUser());
            if (membershipRepository.existsByUserAndSocietyAndStatus(
                    membership.getUser(), membership.getSociety(), MembershipStatus.ACTIVE)) {
                throw new MembershipConflictException("User already has an active membership in this society");
            }
        }
        membership.setStatus(decision);
        membership.setReviewer(admin);
        membership.setReviewedAt(LocalDateTime.now());
        return membershipMapper.toResponse(membershipRepository.save(membership));
    }

    @Transactional
    public MembershipResponse leave(long membershipId, long authenticatedUserId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found"));
        User user = requireActiveResident(authenticatedUserId);
        if (!membership.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Membership does not belong to this user");
        }
        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new MembershipConflictException("Only an active membership can be ended");
        }
        membership.setStatus(MembershipStatus.INACTIVE);
        membership.setEndedAt(LocalDateTime.now());
        return membershipMapper.toResponse(membershipRepository.save(membership));
    }

    public Membership requireActiveMembership(long userId, long societyId) {
        User user = getUser(userId);
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Inactive accounts cannot use society memberships");
        }
        Society society = societyRepository.findById(societyId)
                .orElseThrow(() -> new SocietyNotFoundException("Society not found"));
        return membershipRepository.findFirstByUserAndSocietyAndStatus(user, society, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedException("User has no active membership in this society"));
    }

    private void requireAdminOfSociety(User admin, Society society) {
        if (admin.getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Inactive accounts cannot review membership requests");
        }
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only an admin can review membership requests");
        }
        membershipRepository.findFirstByUserAndSocietyAndStatus(admin, society, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new AccessDeniedException("Admin does not have an active membership in this society"));
    }

    private User requireActiveResident(long userId) {
        User user = getUser(userId);
        requireActiveResident(user);
        return user;
    }

    private void requireActiveResident(User user) {
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Inactive accounts cannot manage resident memberships");
        }
        if (user.getRole() != Role.RESIDENT) {
            throw new AccessDeniedException("Only residents can manage resident memberships");
        }
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}

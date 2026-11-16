package com.pranav.society_vehicle_manager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pranav.society_vehicle_manager.dto.GateRequest;
import com.pranav.society_vehicle_manager.dto.GateResponse;
import com.pranav.society_vehicle_manager.entity.Gate;
import com.pranav.society_vehicle_manager.entity.Society;
import com.pranav.society_vehicle_manager.entity.User;
import com.pranav.society_vehicle_manager.enums.Role;
import com.pranav.society_vehicle_manager.enums.AccountStatus;
import com.pranav.society_vehicle_manager.exception.AccessDeniedException;
import com.pranav.society_vehicle_manager.exception.SocietyNotFoundException;
import com.pranav.society_vehicle_manager.exception.UserNotFoundException;
import com.pranav.society_vehicle_manager.mapper.GateMapper;
import com.pranav.society_vehicle_manager.repository.GateRepository;
import com.pranav.society_vehicle_manager.repository.UserRepository;
import com.pranav.society_vehicle_manager.repository.MembershipRepository;
import com.pranav.society_vehicle_manager.repository.SocietyRepository;
import com.pranav.society_vehicle_manager.enums.MembershipStatus;

@Service 
public class GateService {
    private final GateRepository gateRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final SocietyRepository societyRepository;
    private final GateMapper gateMapper;

    public GateService(GateRepository gateRepository, UserRepository userRepository, MembershipRepository membershipRepository,
            SocietyRepository societyRepository, GateMapper gateMapper) {
        this.gateRepository = gateRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.societyRepository = societyRepository;
        this.gateMapper = gateMapper;
    }

    public GateResponse createGate(long authenticatedAdminId, long societyId, GateRequest request) {
        User admin = userRepository.findById(authenticatedAdminId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (admin.getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Inactive accounts cannot create gates");
        }
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can create gates");
        }

        Society society = societyRepository.findById(societyId)
                .orElseThrow(() -> new SocietyNotFoundException("Society not found"));
        if (!membershipRepository.existsByUserAndSocietyAndStatus(admin, society, MembershipStatus.ACTIVE)) {
            throw new AccessDeniedException("Admin does not have an active membership in this society");
        }

        Gate gate = new Gate();
        gate.setName(request.getName().trim());
        gate.setSociety(society);
        return gateMapper.toResponse(gateRepository.save(gate));
    }

    public List<GateResponse> getGates(long authenticatedUserId, long societyId) {
        // Get the user
        User user = userRepository.findById(authenticatedUserId)
                                  .orElseThrow(() -> new UserNotFoundException("User not found"));
        // check the user role should be a guard

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Inactive accounts cannot view society gates");
        }
        if(user.getRole() != Role.GUARD) {
            throw new AccessDeniedException("User is not a Guard");
        }
        // now get the gates for this guards society

        Society society = societyRepository.findById(societyId)
                .orElseThrow(() -> new SocietyNotFoundException("Society not found"));
        if (!membershipRepository.existsByUserAndSocietyAndStatus(user, society, MembershipStatus.ACTIVE)) {
            throw new AccessDeniedException("Guard does not have an active membership in this society");
        }
        List<Gate> gates = gateRepository.findBySociety(society);
        // map it to gateResponse

        return gates.stream()
             .map(gateMapper::toResponse)
             .toList();
    }

}

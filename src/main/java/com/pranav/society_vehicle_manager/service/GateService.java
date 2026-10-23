package com.pranav.society_vehicle_manager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pranav.society_vehicle_manager.dto.GateResponse;
import com.pranav.society_vehicle_manager.entity.Gate;
import com.pranav.society_vehicle_manager.entity.Society;
import com.pranav.society_vehicle_manager.entity.User;
import com.pranav.society_vehicle_manager.enums.Role;
import com.pranav.society_vehicle_manager.exception.AccessDeniedException;
import com.pranav.society_vehicle_manager.exception.UserNotFoundException;
import com.pranav.society_vehicle_manager.mapper.GateMapper;
import com.pranav.society_vehicle_manager.repository.GateRepository;
import com.pranav.society_vehicle_manager.repository.UserRepository;

@Service 
public class GateService {
    private final GateRepository gateRepository;
    private final UserRepository userRepository;
    private final GateMapper gateMapper;

    public GateService(GateRepository gateRepository, UserRepository userRepository, GateMapper gateMapper) {
        this.gateRepository = gateRepository;
        this.userRepository = userRepository;
        this.gateMapper = gateMapper;
    }

    public List<GateResponse> getGates(long userId) {
        // Get the user
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new UserNotFoundException("User not found"));
        // check the user role should be a guard

        if(user.getRole() != Role.GUARD) {
            throw new AccessDeniedException("User is not a Guard");
        }
        // now get the gates for this guards society

        Society society = user.getSociety();
        List<Gate> gates = gateRepository.findBySociety(society);
        // map it to gateResponse

        return gates.stream()
             .map(gateMapper::toResponse)
             .toList();
    }

}

package com.pranav.society_vehicle_manager.service;

import com.pranav.society_vehicle_manager.repository.EntryExitRecordRepository;

import com.pranav.society_vehicle_manager.repository.GateRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pranav.society_vehicle_manager.dto.EntryExitResponse;
import com.pranav.society_vehicle_manager.dto.VehicleRequest;
import com.pranav.society_vehicle_manager.dto.VehicleResponse;
import com.pranav.society_vehicle_manager.entity.EntryExitRecord;
import com.pranav.society_vehicle_manager.entity.Gate;
import com.pranav.society_vehicle_manager.entity.Membership;
import com.pranav.society_vehicle_manager.entity.Society;
import com.pranav.society_vehicle_manager.entity.User;
import com.pranav.society_vehicle_manager.entity.Vehicle;
import com.pranav.society_vehicle_manager.enums.Role;
import com.pranav.society_vehicle_manager.enums.Status;
import com.pranav.society_vehicle_manager.enums.MembershipStatus;
import com.pranav.society_vehicle_manager.enums.AccountStatus;
import com.pranav.society_vehicle_manager.enums.VehicleLocationStatus;
import com.pranav.society_vehicle_manager.enums.EntryExitType;
import com.pranav.society_vehicle_manager.exception.AccessDeniedException;
import com.pranav.society_vehicle_manager.exception.GateNotFoundException;
import com.pranav.society_vehicle_manager.exception.InvalidStatusException;
import com.pranav.society_vehicle_manager.exception.InvalidVehicleMovementException;
import com.pranav.society_vehicle_manager.exception.UserNotFoundException;
import com.pranav.society_vehicle_manager.exception.VehicleAlreadyExistsException;
import com.pranav.society_vehicle_manager.exception.VehicleAlreadyProcessedException;
import com.pranav.society_vehicle_manager.exception.VehicleNotApprovedException;
import com.pranav.society_vehicle_manager.exception.VehicleNotFoundException;
import com.pranav.society_vehicle_manager.mapper.EntryExitMapper;
import com.pranav.society_vehicle_manager.mapper.VehicleMapper;
import com.pranav.society_vehicle_manager.repository.UserRepository;
import com.pranav.society_vehicle_manager.repository.VehicleRepository;
import com.pranav.society_vehicle_manager.repository.MembershipRepository;
import com.pranav.society_vehicle_manager.repository.SocietyRepository;

import jakarta.transaction.Transactional;

@Service
public class VehicleService {

    private final EntryExitRecordRepository entryExitRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final GateRepository gateRepository;
    private final MembershipRepository membershipRepository;
    private final SocietyRepository societyRepository;

    private final VehicleMapper vehicleMapper;
    private final EntryExitMapper entryExitMapper;

    public VehicleService(VehicleRepository vehicleRepository, UserRepository userRepository,
            EntryExitRecordRepository entryExitRecordRepository,
            GateRepository gateRepository, MembershipRepository membershipRepository, SocietyRepository societyRepository,
            VehicleMapper vehicleMapper, EntryExitMapper entryExitMapper) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.entryExitRecordRepository = entryExitRecordRepository;
        this.gateRepository = gateRepository;
        this.membershipRepository = membershipRepository;
        this.societyRepository = societyRepository;
        this.vehicleMapper = vehicleMapper;
        this.entryExitMapper = entryExitMapper;
    }

    public VehicleResponse findByRegistrationNumber(String registrationNumber) {
        String normalizedRegistrationNumber = registrationNumber.trim().toUpperCase();

        Vehicle vehicle = vehicleRepository
                .findByRegistrationNumber(normalizedRegistrationNumber)
                .orElseThrow(() -> new VehicleNotFoundException(
                        "Vehicle not found " + normalizedRegistrationNumber));

        return vehicleMapper.toResponse(vehicle);
    }

    public VehicleResponse createVehicle(VehicleRequest request, Long membershipId, Long authenticatedUserId) {

        //authenticting user
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new UserNotFoundException("Membership not found"));

        if(!membership.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only register a vehicle for your own membership");
        }

        String registrationNumber = request.getRegistrationNumber().trim().toUpperCase();

        // vehicle already registered or not.
        if (vehicleRepository.existsByRegistrationNumber(registrationNumber)) {
            // reject vehicle
            throw new VehicleAlreadyExistsException("Vehicle already registered " + registrationNumber);
        }

        if (membership.getStatus() != MembershipStatus.ACTIVE || membership.getUser().getRole() != Role.RESIDENT) {
            throw new AccessDeniedException("An active resident membership is required to register a vehicle");
        }
        if (membership.getUser().getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Inactive accounts cannot register vehicles");
        }

        Vehicle vehicle = new Vehicle();

        vehicle.setCompany(request.getCompany());
        vehicle.setRegistrationNumber(registrationNumber);
        vehicle.setModel(request.getModel());
        vehicle.setStatus(Status.PENDING);
        vehicle.setMembership(membership);
        vehicle.setType(request.getType());
        vehicle.setLocationStatus(VehicleLocationStatus.UNKNOWN);

        // need to save this entity into postgreSql
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // Now response fill the VehicleResponse with values from savedvehicle

        return vehicleMapper.toResponse(savedVehicle);

    }

    public VehicleResponse updateVehicleStatus(String registrationNumber, long authenticatedUserId, long societyId, Status updateStatus) {
        String normalizedRegistrationNumber = registrationNumber.trim().toUpperCase();

        // checking whether the user is ADMIN or not
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getRole().equals(Role.ADMIN)) {
            // throw exception that this user is not authorized to do so.
            throw new AccessDeniedException("User is not Authorized to update the status");
        }

        Vehicle vehicle = vehicleRepository
                .findByRegistrationNumber(normalizedRegistrationNumber)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found " + normalizedRegistrationNumber));

        if (vehicle.getStatus() != Status.PENDING) {
            throw new VehicleAlreadyProcessedException("Vehicle Request has already been processed");
        }

        //check vehicle belongs to the admins's society
        Society adminSociety = societyRepository.findById(societyId)
                .orElseThrow(() -> new UserNotFoundException("Society not found"));
        if (!membershipRepository.existsByUserAndSocietyAndStatus(user, adminSociety, MembershipStatus.ACTIVE)) {
            throw new AccessDeniedException("Admin does not have an active membership in this society");
        }
        if(!vehicle.getMembership().getSociety().getId().equals(societyId)) {
            throw new AccessDeniedException("Vehicle Doesn't belongs to the admin's Society");
        }

        if (updateStatus == Status.APPROVED) {
            vehicle.setStatus(Status.APPROVED);
        } else if (updateStatus == Status.REJECTED) {
            vehicle.setStatus(Status.REJECTED);
        } else {
            throw new InvalidStatusException("Status in not valid");
        }

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return vehicleMapper.toResponse(savedVehicle);
    }

    public List<VehicleResponse> getPendingVehicle(long authenticatedUserId, long societyId) {

        // whether
        User user = userRepository
                .findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getRole().equals(Role.ADMIN)) {
            // user is not an admin
            throw new AccessDeniedException("User is not Authorized");
        }

        Society society = societyRepository.findById(societyId)
                .orElseThrow(() -> new UserNotFoundException("Society not found"));
        if (!membershipRepository.existsByUserAndSocietyAndStatus(user, society, MembershipStatus.ACTIVE)) {
            throw new AccessDeniedException("Admin does not have an active membership in this society");
        }

        // get Pending vehicles
        List<Vehicle> pendingVehicles = vehicleRepository.findByMembership_SocietyAndStatusAndMembership_Status(
                society, Status.PENDING, MembershipStatus.ACTIVE);

        // put all the vehicles in response because that is what the admin will see

        return pendingVehicles.stream()
                        .map(vehicleMapper::toResponse)
                        .toList();
    }

    @Transactional
    public EntryExitResponse processVehicleEntryExit(String registrationNumber, Long authenticatedUserId,
            EntryExitType entryExitType, long gateId) {
        // checking whether the user is GUARD or not
        String normalizedRegistrationNumber = registrationNumber.trim().toUpperCase();
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if(user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Inactive accounts cannot process vehicle entry/exit");
        }

        if (user.getRole() != Role.GUARD) {
            // user is not an GUARD
            throw new AccessDeniedException("User is not Authorized to process the vehicle entry/exit");
        }

        Vehicle vehicle = vehicleRepository
                .findByRegistrationNumber(normalizedRegistrationNumber)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found " + normalizedRegistrationNumber));

        if (vehicle.getStatus() != Status.APPROVED) {
            throw new VehicleNotApprovedException("Vehicle Request has not been approved");
        }

        Society society = vehicle.getMembership().getSociety();
        Membership guardMembership = membershipRepository.findFirstByUserAndSocietyAndStatus(user, society,
                MembershipStatus.ACTIVE).orElseThrow(() -> new AccessDeniedException(
                        "Guard has no active membership in society : " + society.getName()));
        if (vehicle.getMembership().getStatus() != MembershipStatus.ACTIVE) {
            throw new VehicleNotApprovedException("Vehicle owner's membership is no longer active");
        }

        // checking the gate id is valid or not and the gate belongs to the society
        Gate gate = gateRepository.findById(gateId)
                .orElseThrow(() -> new GateNotFoundException("Gate not found " + gateId));

        if (!gate.getSociety().getId().equals(society.getId())) {
            throw new AccessDeniedException("Gate does not belong to society : " + society.getName());
        }

        // checking the location status of the vehicle
        if (EntryExitType.ENTRY.equals(entryExitType)) {
            if (VehicleLocationStatus.INSIDE.equals(vehicle.getLocationStatus())) {
                throw new InvalidVehicleMovementException("Vehicle is already inside the society");
            }
            vehicle.setLocationStatus(VehicleLocationStatus.INSIDE);

        } else if (EntryExitType.EXIT.equals(entryExitType)) {
            if (VehicleLocationStatus.OUTSIDE.equals(vehicle.getLocationStatus())) {
                throw new InvalidVehicleMovementException("Vehicle is already outside the society");
            }
            vehicle.setLocationStatus(VehicleLocationStatus.OUTSIDE);
        } else {
            throw new InvalidVehicleMovementException("Invalid entry/exit type");
        }

        EntryExitRecord entryExitRecord = new EntryExitRecord();
        entryExitRecord.setVehicle(vehicle);
        entryExitRecord.setGate(gate);
        entryExitRecord.setType(entryExitType);
        entryExitRecord.setTimestamp(LocalDateTime.now());
        entryExitRecord.setGuard(user);

        EntryExitRecord savedEntryExitRecord = entryExitRecordRepository.save(entryExitRecord);

        return entryExitMapper.toResponse(savedEntryExitRecord);
    }

    public List<VehicleResponse> myVehicles(Long authenticatedUserId) {
        //Get the user
        User user = userRepository.findById(authenticatedUserId)
                                  .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getRole() != Role.RESIDENT || user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("An active resident account is required");
        }
        //Optimize it by calling the vehicle user and flat from user in Join fetch JPQL query.
        List<Vehicle> vehicles = vehicleRepository.findByMembership_User(user);


        return vehicles.stream()
                  .map(vehicleMapper::toResponse)
                  .toList();
    }
}

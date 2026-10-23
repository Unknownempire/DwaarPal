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
import com.pranav.society_vehicle_manager.entity.Society;
import com.pranav.society_vehicle_manager.entity.User;
import com.pranav.society_vehicle_manager.entity.Vehicle;
import com.pranav.society_vehicle_manager.enums.Role;
import com.pranav.society_vehicle_manager.enums.Status;
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

import jakarta.transaction.Transactional;

@Service
public class VehicleService {

    private final EntryExitRecordRepository entryExitRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final GateRepository gateRepository;

    private final VehicleMapper vehicleMapper;
    private final EntryExitMapper entryExitMapper;

    public VehicleService(VehicleRepository vehicleRepository, UserRepository userRepository,
            EntryExitRecordRepository entryExitRecordRepository,
            GateRepository gateRepository, VehicleMapper vehicleMapper, EntryExitMapper entryExitMapper) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.entryExitRecordRepository = entryExitRecordRepository;
        this.gateRepository = gateRepository;
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

    public VehicleResponse createVehicle(VehicleRequest request, Long userId) {

        String registrationNumber = request.getRegistrationNumber().trim().toUpperCase();

        // vehicle already registered or not.
        if (vehicleRepository.existsByRegistrationNumber(registrationNumber)) {
            // reject vehicle
            throw new VehicleAlreadyExistsException("Vehicle already registered " + registrationNumber);
        }

        // vehicle not registered
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Vehicle vehicle = new Vehicle();

        vehicle.setCompany(request.getCompany());
        vehicle.setRegistrationNumber(registrationNumber);
        vehicle.setModel(request.getModel());
        vehicle.setOwner(user);
        vehicle.setStatus(Status.PENDING);
        vehicle.setSociety(user.getSociety());
        vehicle.setType(request.getType());
        vehicle.setLocationStatus(VehicleLocationStatus.UNKNOWN);

        // need to save this entity into postgreSql
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // Now response fill the VehicleResponse with values from savedvehicle

        return vehicleMapper.toResponse(savedVehicle);

    }

    public VehicleResponse updateVehicleStatus(String registrationNumber, long userId, Status updateStatus) {
        String normalizedRegistrationNumber = registrationNumber.trim().toUpperCase();

        // checking whether the user is ADMIN or not
        User user = userRepository.findById(userId)
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
        if(!vehicle.getSociety().getId().equals(user.getSociety().getId())) {
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

    public List<VehicleResponse> getPendingVehicle(long userId) {

        // whether
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getRole().equals(Role.ADMIN)) {
            // user is not an admin
            throw new AccessDeniedException("User is not Authorized");
        }

        Society society = user.getSociety();

        // get Pending vehicles
        List<Vehicle> pendingVehicles = vehicleRepository.findBySocietyAndStatus(society, Status.PENDING);

        // put all the vehicles in response because that is what the admin will see

        return pendingVehicles.stream()
                        .map(vehicleMapper::toResponse)
                        .toList();
    }

    @Transactional
    public EntryExitResponse processVehicleEntryExit(String registrationNumber, long userId,
            EntryExitType entryExitType, long gateId) {
        // checking whether the user is GUARD or not
        String normalizedRegistrationNumber = registrationNumber.trim().toUpperCase();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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

        // checking the the society of the vehicle and the user
        if (!vehicle.getSociety().getId().equals(user.getSociety().getId())) {
            throw new AccessDeniedException("Vehicle does not belong to society : " + user.getSociety().getName());
        }

        // checking the gate id is valid or not and the gate belongs to the society
        Gate gate = gateRepository.findById(gateId)
                .orElseThrow(() -> new GateNotFoundException("Gate not found " + gateId));

        if (!gate.getSociety().getId().equals(user.getSociety().getId())) {
            throw new AccessDeniedException("Gate does not belong to society : " + user.getSociety().getName());
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

    public List<VehicleResponse> myVehicles(long userId) {
        //Get the user
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Vehicle> vehicles = vehicleRepository.findByOwner(user);


        return vehicles.stream()
                  .map(vehicleMapper::toResponse)
                  .toList();
    }
}

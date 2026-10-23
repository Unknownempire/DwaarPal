package com.pranav.society_vehicle_manager.service;

import java.util.List;

import javax.management.RuntimeErrorException;

import org.springframework.stereotype.Service;

import com.pranav.society_vehicle_manager.dto.EntryExitResponse;
import com.pranav.society_vehicle_manager.entity.EntryExitRecord;
import com.pranav.society_vehicle_manager.entity.Society;
import com.pranav.society_vehicle_manager.entity.User;
import com.pranav.society_vehicle_manager.entity.Vehicle;
import com.pranav.society_vehicle_manager.enums.Role;
import com.pranav.society_vehicle_manager.exception.AccessDeniedException;
import com.pranav.society_vehicle_manager.exception.UserNotFoundException;
import com.pranav.society_vehicle_manager.exception.VehicleNotFoundException;
import com.pranav.society_vehicle_manager.mapper.EntryExitMapper;
import com.pranav.society_vehicle_manager.repository.EntryExitRecordRepository;
import com.pranav.society_vehicle_manager.repository.UserRepository;
import com.pranav.society_vehicle_manager.repository.VehicleRepository;

@Service
public class EntryExitService {
    private final EntryExitRecordRepository entryExitRecordRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final EntryExitMapper entryExitMapper;

    public EntryExitService(EntryExitRecordRepository entryExitRecordRepository, UserRepository userRepository, VehicleRepository vehicleRepository, EntryExitMapper entryExitMapper) {
        this.entryExitRecordRepository = entryExitRecordRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this. entryExitMapper = entryExitMapper;
    }

    public List<EntryExitResponse> getSocietyEntryExitHistory(long userId) {
        // get the user;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // user should be guard or Admin to veiw the history
        if (user.getRole() != Role.ADMIN &&
                user.getRole() != Role.GUARD) {
            throw new AccessDeniedException(
                    "Only Admins and Guards can view entry/exit history");
        }

        // get the society
        Society society = user.getSociety();

        // get the records of the vehicles in the entryexitrecords that have this society
        List<EntryExitRecord> entryExitRecords = entryExitRecordRepository.findByVehicle_SocietyOrderByTimestampDesc(society);

        return entryExitRecords.stream()
                .map(entryExitMapper::toResponse)
                .toList();

    }
    public List<EntryExitResponse> getVehicleEntryExitHistory(long userId, String registrationNumber) {
        // get the user;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // check whether vehicle exists or not
        Vehicle vehicle = vehicleRepository.findByRegistrationNumber(registrationNumber)
                                           .orElseThrow(() -> new VehicleNotFoundException("No Vehicle exists with this " + registrationNumber));
        // user should be the owner of the vehicle or the Admin of the society.

        //society should be same
        if(vehicle.getSociety() != user.getSociety()) {
            throw new RuntimeException("Vehicle and user society is different");
        }

        boolean isOwner = vehicle.getOwner() == user ? true : false;
        boolean isAdminOfSameSociety = user.getRole() == Role.ADMIN ? true : false;

        if(!isOwner && !isAdminOfSameSociety) {
            throw new RuntimeException("User not Authorized to view the history of the vehicle " + registrationNumber);
        }

        // get the records of the vehicles in the entryexitrecords that have this society
        List<EntryExitRecord> entryExitRecords = entryExitRecordRepository.findByVehicle_RegistrationNumberOrderByTimestampDesc(registrationNumber);

        return entryExitRecords.stream()
                .map(entryExitMapper::toResponse)
                .toList();

    }

}

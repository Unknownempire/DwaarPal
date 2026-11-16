package com.pranav.society_vehicle_manager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pranav.society_vehicle_manager.dto.EntryExitResponse;
import com.pranav.society_vehicle_manager.entity.EntryExitRecord;
import com.pranav.society_vehicle_manager.entity.Society;
import com.pranav.society_vehicle_manager.entity.User;
import com.pranav.society_vehicle_manager.entity.Vehicle;
import com.pranav.society_vehicle_manager.entity.Membership;
import com.pranav.society_vehicle_manager.enums.MembershipStatus;
import com.pranav.society_vehicle_manager.enums.AccountStatus;
import com.pranav.society_vehicle_manager.enums.Role;
import com.pranav.society_vehicle_manager.exception.AccessDeniedException;
import com.pranav.society_vehicle_manager.exception.UserNotFoundException;
import com.pranav.society_vehicle_manager.exception.VehicleNotFoundException;
import com.pranav.society_vehicle_manager.mapper.EntryExitMapper;
import com.pranav.society_vehicle_manager.repository.EntryExitRecordRepository;
import com.pranav.society_vehicle_manager.repository.UserRepository;
import com.pranav.society_vehicle_manager.repository.VehicleRepository;
import com.pranav.society_vehicle_manager.repository.MembershipRepository;

@Service
public class EntryExitService {
    private final EntryExitRecordRepository entryExitRecordRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final EntryExitMapper entryExitMapper;
    private final MembershipRepository membershipRepository;

    public EntryExitService(EntryExitRecordRepository entryExitRecordRepository, UserRepository userRepository,
            VehicleRepository vehicleRepository, MembershipRepository membershipRepository, EntryExitMapper entryExitMapper) {
        this.entryExitRecordRepository = entryExitRecordRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.membershipRepository = membershipRepository;
        this. entryExitMapper = entryExitMapper;
    }

    public List<EntryExitResponse> getSocietyEntryExitHistory(long authenticatedUserId) {
        // get the user;
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Inactive accounts cannot view entry/exit history");
        }

        // user should be guard or Admin to veiw the history
        if (user.getRole() != Role.ADMIN &&
                user.getRole() != Role.GUARD) {
            throw new AccessDeniedException(
                    "Only Admins and Guards can view entry/exit history");
        }

        List<Society> societies = membershipRepository.findByUserAndStatus(user, MembershipStatus.ACTIVE).stream()
                .map(Membership::getSociety).distinct().toList();
        return societies.stream()
                .flatMap(society -> entryExitRecordRepository
                        .findByVehicle_Membership_SocietyOrderByTimestampDesc(society).stream())
                .map(entryExitMapper::toResponse)
                .toList();

    }
    public List<EntryExitResponse> getVehicleEntryExitHistory(long authenticatedUserId, String registrationNumber) {
        // get the user;
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Inactive accounts cannot view vehicle history");
        }

        // check whether vehicle exists or not
        String normalizedRegistrationNumber = registrationNumber.trim().toUpperCase();
        Vehicle vehicle = vehicleRepository.findByRegistrationNumber(normalizedRegistrationNumber)
                .orElseThrow(() -> new VehicleNotFoundException(
                        "No vehicle exists with this " + normalizedRegistrationNumber));
        // The owner or active society staff member may view this vehicle's history.

        Society society = vehicle.getMembership().getSociety();
        boolean isOwner = user.getRole() == Role.RESIDENT
                && vehicle.getMembership().getUser().getId().equals(user.getId());
        boolean isAdminOfSameSociety = user.getRole() == Role.ADMIN
                && membershipRepository.existsByUserAndSocietyAndStatus(user, society, MembershipStatus.ACTIVE);
        boolean isGuardOfSameSociety = user.getRole() == Role.GUARD
                && membershipRepository.existsByUserAndSocietyAndStatus(user, society, MembershipStatus.ACTIVE);

        if (!isOwner && !isAdminOfSameSociety && !isGuardOfSameSociety) {
            throw new AccessDeniedException("User not authorized to view the vehicle history");
        }

        // get the records of the vehicles in the entryexitrecords that have this society
        List<EntryExitRecord> entryExitRecords = entryExitRecordRepository
                .findByVehicle_RegistrationNumberOrderByTimestampDesc(normalizedRegistrationNumber);

        return entryExitRecords.stream()
                .map(entryExitMapper::toResponse)
                .toList();

    }

}

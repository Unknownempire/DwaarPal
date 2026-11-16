package com.pranav.society_vehicle_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranav.society_vehicle_manager.entity.Membership;
import com.pranav.society_vehicle_manager.entity.Society;
import com.pranav.society_vehicle_manager.entity.User;
import com.pranav.society_vehicle_manager.entity.Vehicle;
import com.pranav.society_vehicle_manager.enums.Status;

public interface VehicleRepository extends JpaRepository<Vehicle,Long>{
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    List<Vehicle> findByMembership_SocietyAndStatus(Society society, Status status);

    //find vehicle based on userId
    List<Vehicle> findByMembership_User(User owner);

    List<Vehicle> findByMembershipIn(List<Membership> memberships);

    List<Vehicle> findByMembership_SocietyAndStatusAndMembership_Status(Society society, Status status,
            com.pranav.society_vehicle_manager.enums.MembershipStatus membershipStatus);


}

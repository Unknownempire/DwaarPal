package com.pranav.society_vehicle_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranav.society_vehicle_manager.entity.Society;
import com.pranav.society_vehicle_manager.entity.User;
import com.pranav.society_vehicle_manager.entity.Vehicle;
import com.pranav.society_vehicle_manager.enums.Status;

public interface VehicleRepository extends JpaRepository<Vehicle,Long>{
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    List<Vehicle> findBySocietyAndStatus(Society society, Status status);

    //find vehicle based on userId
    List<Vehicle> findByOwner(User owner);


}

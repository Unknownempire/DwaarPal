package com.pranav.society_vehicle_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranav.society_vehicle_manager.entity.EntryExitRecord;
import com.pranav.society_vehicle_manager.entity.Society;

import java.util.List;


public interface EntryExitRecordRepository extends JpaRepository<EntryExitRecord,Long>{
    List<EntryExitRecord> findByVehicle_SocietyOrderByTimestampDesc(Society society);
    List<EntryExitRecord> findByVehicle_RegistrationNumberOrderByTimestampDesc(
        String registrationNumber);

}

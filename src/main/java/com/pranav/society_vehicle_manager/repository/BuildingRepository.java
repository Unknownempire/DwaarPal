package com.pranav.society_vehicle_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranav.society_vehicle_manager.entity.Building;
import java.util.List;

public interface BuildingRepository extends JpaRepository<Building, Long>{
    List<Building> findBySociety_IdOrderByIdAsc(Long societyId);

}

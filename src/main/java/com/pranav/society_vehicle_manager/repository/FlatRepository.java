package com.pranav.society_vehicle_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranav.society_vehicle_manager.entity.Flat;
import java.util.List;

public interface FlatRepository extends JpaRepository<Flat,Long>{
    List<Flat> findByBuilding_IdOrderByFlatNumberAsc(Long buildingId);

}

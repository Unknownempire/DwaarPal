package com.pranav.society_vehicle_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranav.society_vehicle_manager.entity.Gate;
import com.pranav.society_vehicle_manager.entity.Society;

import java.util.List;


public interface GateRepository extends JpaRepository<Gate,Long>{
    List<Gate> findBySociety(Society society);

}

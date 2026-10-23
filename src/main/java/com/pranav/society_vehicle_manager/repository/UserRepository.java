package com.pranav.society_vehicle_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranav.society_vehicle_manager.entity.User;

public interface UserRepository extends JpaRepository<User,Long>{

}

package com.pranav.society_vehicle_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranav.society_vehicle_manager.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>{
    boolean existsByPhone(String phone);
    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByPhone(String phone);
}

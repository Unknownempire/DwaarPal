package com.pranav.society_vehicle_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pranav.society_vehicle_manager.entity.Membership;
import com.pranav.society_vehicle_manager.entity.Society;
import com.pranav.society_vehicle_manager.entity.User;
import com.pranav.society_vehicle_manager.enums.MembershipStatus;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    boolean existsByUserAndSocietyAndStatus(User user, Society society, MembershipStatus status);

    Optional<Membership> findFirstByUserAndSocietyAndStatus(User user, Society society, MembershipStatus status);

    List<Membership> findByUserOrderByAppliedAtDesc(User user);

    List<Membership> findBySocietyAndStatusOrderByAppliedAtAsc(Society society, MembershipStatus status);

    List<Membership> findByUserAndStatus(User user, MembershipStatus status);
}

package com.pranav.society_vehicle_manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.pranav.society_vehicle_manager.enums.Role;
import com.pranav.society_vehicle_manager.enums.AccountStatus;

@Entity 
@Table(name = "users")
@Getter 
@Setter 
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

}

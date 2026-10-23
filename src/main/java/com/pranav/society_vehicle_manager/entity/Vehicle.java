package com.pranav.society_vehicle_manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.pranav.society_vehicle_manager.enums.VehicleType;
import com.pranav.society_vehicle_manager.enums.Status;
import com.pranav.society_vehicle_manager.enums.VehicleLocationStatus;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String registrationNumber;

    @Column
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    private VehicleLocationStatus locationStatus;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;
}
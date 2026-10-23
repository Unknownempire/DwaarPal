package com.pranav.society_vehicle_manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "flats")
@Getter
@Setter
public class Flat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String flatNumber;

    @ManyToOne
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    
}

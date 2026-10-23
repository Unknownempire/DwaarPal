package com.pranav.society_vehicle_manager.entity;

import com.pranav.society_vehicle_manager.enums.EntryExitType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "entry_exit_records")
@Getter
@Setter
public class EntryExitRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "guard_id", nullable = false)
    private User guard;

    @ManyToOne
    @JoinColumn(name = "gate_id", nullable = false)
    private Gate gate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryExitType type;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
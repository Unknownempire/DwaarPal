package com.pranav.society_vehicle_manager.dto;

import java.time.LocalDateTime;

import com.pranav.society_vehicle_manager.enums.EntryExitType;
import com.pranav.society_vehicle_manager.enums.VehicleLocationStatus;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class EntryExitResponse {
    private Long id;
    private String registrationNumber;
    private String gateName;
    private EntryExitType type;
    private LocalDateTime timestamp;
    private VehicleLocationStatus currentLocation;
}

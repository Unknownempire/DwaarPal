package com.pranav.society_vehicle_manager.dto;

import com.pranav.society_vehicle_manager.enums.VehicleType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class VehicleRequest {
    @NotBlank 
    private String registrationNumber;

    @NotNull 
    private VehicleType type;

    @NotBlank 
    private String company;

    @NotBlank 
    private String model;
}

package com.pranav.society_vehicle_manager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GateRequest {
    @NotBlank
    private String name;
}

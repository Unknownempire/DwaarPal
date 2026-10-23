package com.pranav.society_vehicle_manager.mapper;

import org.springframework.stereotype.Component;

import com.pranav.society_vehicle_manager.dto.GateResponse;
import com.pranav.society_vehicle_manager.entity.Gate;

@Component 
public class GateMapper {
    public GateResponse toResponse(Gate gate) {
        GateResponse response = new GateResponse();

        response.setId(gate.getId());
        response.setName(gate.getName());

        return response;
    }

}

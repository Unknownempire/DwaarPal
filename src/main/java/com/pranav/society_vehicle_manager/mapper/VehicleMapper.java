package com.pranav.society_vehicle_manager.mapper;

import org.springframework.stereotype.Component;

import com.pranav.society_vehicle_manager.dto.VehicleResponse;
import com.pranav.society_vehicle_manager.entity.Vehicle;

@Component 
public class VehicleMapper {
    public VehicleResponse toResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();

        response.setRegistrationNumber(vehicle.getRegistrationNumber());
        response.setType(vehicle.getType());
        response.setCompany(vehicle.getCompany());
        response.setModel(vehicle.getModel());
        response.setStatus(vehicle.getStatus());
        response.setOwnerName(vehicle.getOwner() == null ? null : vehicle.getOwner().getName());
        response.setFlatNumber(vehicle.getOwner().getFlat() == null ? null : vehicle.getOwner().getFlat().getFlatNumber());

        return response;
    }

}

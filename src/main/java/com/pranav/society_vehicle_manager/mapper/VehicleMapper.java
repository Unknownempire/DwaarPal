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
        response.setOwnerName(vehicle.getMembership().getUser().getName());
        response.setFlatNumber(vehicle.getMembership().getFlat() == null ? null : vehicle.getMembership().getFlat().getFlatNumber());
        response.setMembershipId(vehicle.getMembership().getId());
        response.setMembershipStatus(vehicle.getMembership().getStatus());

        return response;
    }

}

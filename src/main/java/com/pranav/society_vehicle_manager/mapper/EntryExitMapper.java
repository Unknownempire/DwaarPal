package com.pranav.society_vehicle_manager.mapper;

import org.springframework.stereotype.Component;

import com.pranav.society_vehicle_manager.dto.EntryExitResponse;
import com.pranav.society_vehicle_manager.entity.EntryExitRecord;

@Component
public class EntryExitMapper {
    public EntryExitResponse toResponse(EntryExitRecord record) {
        EntryExitResponse response = new EntryExitResponse();

        response.setId(record.getId());
        response.setRegistrationNumber(record.getVehicle().getRegistrationNumber());
        response.setGateName(record.getGate().getName());
        response.setType(record.getType());
        response.setTimestamp(record.getTimestamp());
        response.setCurrentLocation(record.getVehicle().getLocationStatus());

        return response;
    }

}

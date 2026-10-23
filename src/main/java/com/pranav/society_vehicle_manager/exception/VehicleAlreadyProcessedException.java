package com.pranav.society_vehicle_manager.exception;

public class VehicleAlreadyProcessedException extends RuntimeException{
    public VehicleAlreadyProcessedException(String message) {
        super(message);
    }

}

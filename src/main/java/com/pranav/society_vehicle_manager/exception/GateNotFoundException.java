package com.pranav.society_vehicle_manager.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * GateNotFoundException
 */
@Getter 
@Setter 
public class GateNotFoundException extends RuntimeException{
    public GateNotFoundException(String message) {
        super(message);
    }

}

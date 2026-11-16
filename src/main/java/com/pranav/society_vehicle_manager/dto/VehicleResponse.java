package com.pranav.society_vehicle_manager.dto;

import com.pranav.society_vehicle_manager.enums.Status;
import com.pranav.society_vehicle_manager.enums.VehicleType;
import com.pranav.society_vehicle_manager.enums.MembershipStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleResponse {
    private String registrationNumber;
    private VehicleType type;
    private String company;
    private String model;
    private Status status;
    private String ownerName;
    private String flatNumber;
    private Long membershipId;
    private MembershipStatus membershipStatus;

}

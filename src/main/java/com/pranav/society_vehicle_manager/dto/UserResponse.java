package com.pranav.society_vehicle_manager.dto;

import com.pranav.society_vehicle_manager.enums.AccountStatus;
import com.pranav.society_vehicle_manager.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private Role role;
    private AccountStatus status;
}

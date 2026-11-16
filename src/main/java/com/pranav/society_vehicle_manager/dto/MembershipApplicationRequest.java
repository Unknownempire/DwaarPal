package com.pranav.society_vehicle_manager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipApplicationRequest {
    @NotNull
    private Long societyId;

    @NotNull
    private Long flatId;
}

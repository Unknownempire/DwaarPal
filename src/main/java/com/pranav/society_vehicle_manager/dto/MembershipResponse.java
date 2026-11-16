package com.pranav.society_vehicle_manager.dto;

import java.time.LocalDateTime;

import com.pranav.society_vehicle_manager.enums.MembershipStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long societyId;
    private String societyName;
    private Long flatId;
    private String flatNumber;
    private MembershipStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime endedAt;
    private Long reviewerUserId;
    private String reviewerName;
}

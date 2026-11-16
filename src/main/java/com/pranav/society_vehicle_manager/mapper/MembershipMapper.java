package com.pranav.society_vehicle_manager.mapper;

import org.springframework.stereotype.Component;

import com.pranav.society_vehicle_manager.dto.MembershipResponse;
import com.pranav.society_vehicle_manager.entity.Membership;

@Component
public class MembershipMapper {
    public MembershipResponse toResponse(Membership membership) {
        MembershipResponse response = new MembershipResponse();
        response.setId(membership.getId());
        response.setUserId(membership.getUser().getId());
        response.setUserName(membership.getUser().getName());
        response.setSocietyId(membership.getSociety().getId());
        response.setSocietyName(membership.getSociety().getName());
        if (membership.getFlat() != null) {
            response.setFlatId(membership.getFlat().getId());
            response.setFlatNumber(membership.getFlat().getFlatNumber());
        }
        response.setStatus(membership.getStatus());
        response.setAppliedAt(membership.getAppliedAt());
        response.setReviewedAt(membership.getReviewedAt());
        response.setEndedAt(membership.getEndedAt());
        if (membership.getReviewer() != null) {
            response.setReviewerUserId(membership.getReviewer().getId());
            response.setReviewerName(membership.getReviewer().getName());
        }
        return response;
    }
}

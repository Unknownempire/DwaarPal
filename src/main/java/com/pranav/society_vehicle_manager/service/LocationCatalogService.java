package com.pranav.society_vehicle_manager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pranav.society_vehicle_manager.dto.BuildingOption;
import com.pranav.society_vehicle_manager.dto.FlatOption;
import com.pranav.society_vehicle_manager.dto.SocietyOption;
import com.pranav.society_vehicle_manager.repository.BuildingRepository;
import com.pranav.society_vehicle_manager.repository.FlatRepository;
import com.pranav.society_vehicle_manager.repository.SocietyRepository;

@Service
public class LocationCatalogService {
    private final SocietyRepository societyRepository;
    private final BuildingRepository buildingRepository;
    private final FlatRepository flatRepository;

    public LocationCatalogService(SocietyRepository societyRepository, BuildingRepository buildingRepository,
            FlatRepository flatRepository) {
        this.societyRepository = societyRepository;
        this.buildingRepository = buildingRepository;
        this.flatRepository = flatRepository;
    }

    public List<SocietyOption> societies() {
        return societyRepository.findAll().stream()
                .map(s -> new SocietyOption(s.getId(), s.getName(), s.getCity(), s.getState())).toList();
    }

    public List<BuildingOption> buildings(long societyId) {
        return buildingRepository.findBySociety_IdOrderByIdAsc(societyId).stream()
                .map(b -> new BuildingOption(b.getId(), b.getName(), b.getSociety().getId())).toList();
    }

    public List<FlatOption> flats(long buildingId) {
        return flatRepository.findByBuilding_IdOrderByFlatNumberAsc(buildingId).stream()
                .map(f -> new FlatOption(f.getId(), f.getFlatNumber(), f.getBuilding().getId())).toList();
    }
}

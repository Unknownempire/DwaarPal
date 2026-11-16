package com.pranav.society_vehicle_manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pranav.society_vehicle_manager.dto.BuildingOption;
import com.pranav.society_vehicle_manager.dto.FlatOption;
import com.pranav.society_vehicle_manager.dto.SocietyOption;
import com.pranav.society_vehicle_manager.service.LocationCatalogService;

@RestController
@RequestMapping("/api")
public class LocationCatalogController {
    private final LocationCatalogService locationCatalogService;

    public LocationCatalogController(LocationCatalogService locationCatalogService) {
        this.locationCatalogService = locationCatalogService;
    }

    @GetMapping("/societies")
    public ResponseEntity<List<SocietyOption>> societies() {
        return ResponseEntity.ok(locationCatalogService.societies());
    }

    @GetMapping("/societies/{societyId}/buildings")
    public ResponseEntity<List<BuildingOption>> buildings(@PathVariable long societyId) {
        return ResponseEntity.ok(locationCatalogService.buildings(societyId));
    }

    @GetMapping("/buildings/{buildingId}/flats")
    public ResponseEntity<List<FlatOption>> flats(@PathVariable long buildingId) {
        return ResponseEntity.ok(locationCatalogService.flats(buildingId));
    }
}

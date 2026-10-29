package com.mmu.shuttle.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ActiveBusModel {
    private Long id;
    private Long routeId;
    private Long driverId;
    private Long vehicleId;
    private String busPlate;
    private LocationModel location;
    private Long nextBusRouteStationId;
    private int nextSequence;
    private boolean active = true;
    private String color;
    private boolean isAtStation;
    private Long lastVisitedRouteStationId;
    private Map<Long, Integer> etas = new ConcurrentHashMap<>();
    private long lastEtaCalculationTime = 0L;
    private int currentOccupancy = 0;
    private int maxCapacity = 40;
}

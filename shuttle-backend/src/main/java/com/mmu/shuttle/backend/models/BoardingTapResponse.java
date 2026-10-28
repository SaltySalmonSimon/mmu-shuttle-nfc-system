package com.mmu.shuttle.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardingTapResponse {
    private String status;           // SUCCESS, UNREGISTERED, DEBOUNCED, INVALID
    private String message;
    private String cardUid;
    private String studentMatrixId;
    private String studentName;
    private String faculty;
    private Long vehicleId;
    private String detectedStation;
    private int currentOccupancy;
    private int maxCapacity;
    private long cooldownRemainingMs;
}


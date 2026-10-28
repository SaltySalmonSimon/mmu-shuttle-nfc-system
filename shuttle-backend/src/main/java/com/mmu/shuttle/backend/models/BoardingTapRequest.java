package com.mmu.shuttle.backend.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardingTapRequest {
    private String cardUid;
    private Long vehicleId;
    private Double latitude;
    private Double longitude;
    private Long timestamp;
}


package com.mmu.shuttle.backend.controllers;

import com.mmu.shuttle.backend.models.BoardingTapRequest;
import com.mmu.shuttle.backend.models.BoardingTapResponse;
import com.mmu.shuttle.backend.services.BoardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boarding")
@CrossOrigin(origins = "*")
public class BoardingController {

    @Autowired
    private BoardingService boardingService;

    /**
     * Ingestion endpoint for on-bus NFC reader tap
     */
    @PostMapping("/tap")
    public ResponseEntity<BoardingTapResponse> handleCardTap(@RequestBody BoardingTapRequest request) {
        BoardingTapResponse response = boardingService.processCardTap(request);

        if ("DEBOUNCED".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
        } else if ("INVALID".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else if ("UNREGISTERED".equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get live occupancy of a vehicle
     */
    @GetMapping("/occupancy/{vehicleId}")
    public ResponseEntity<Integer> getOccupancy(@PathVariable Long vehicleId) {
        int count = boardingService.getVehicleOccupancy(vehicleId);
        return ResponseEntity.ok(count);
    }

    /**
     * Driver or supervisor resets occupancy count for route terminus
     */
    @PostMapping("/reset/{vehicleId}")
    public ResponseEntity<String> resetOccupancy(@PathVariable Long vehicleId) {
        boardingService.resetVehicleOccupancy(vehicleId);
        return ResponseEntity.ok("Occupancy reset successfully for vehicle " + vehicleId);
    }
}


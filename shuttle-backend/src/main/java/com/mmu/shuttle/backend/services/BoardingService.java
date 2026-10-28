package com.mmu.shuttle.backend.services;

import com.mmu.shuttle.backend.entities.ShuttleBoardingLog;
import com.mmu.shuttle.backend.entities.Station;
import com.mmu.shuttle.backend.entities.StudentCard;
import com.mmu.shuttle.backend.entities.Vehicle;
import com.mmu.shuttle.backend.models.BoardingTapRequest;
import com.mmu.shuttle.backend.models.BoardingTapResponse;
import com.mmu.shuttle.backend.repositories.ShuttleBoardingLogRepository;
import com.mmu.shuttle.backend.repositories.StationRepository;
import com.mmu.shuttle.backend.repositories.StudentCardRepository;
import com.mmu.shuttle.backend.repositories.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@Service
@Slf4j
public class BoardingService {

    private static final Pattern HEX_UID_PATTERN = Pattern.compile("^[0-9A-Fa-f]{4,32}$");
    private static final long DEBOUNCE_COOLDOWN_MS = 5000L; // 5-second per-card sliding window
    private static final int DEFAULT_MAX_CAPACITY = 40;

    private final ConcurrentHashMap<String, Long> lastTapTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AtomicInteger> vehicleOccupancyMap = new ConcurrentHashMap<>();

    @Autowired
    private StudentCardRepository studentCardRepository;

    @Autowired
    private ShuttleBoardingLogRepository boardingLogRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Process an incoming smart card tap from on-bus reader
     */
    public BoardingTapResponse processCardTap(BoardingTapRequest request) {
        if (request == null || request.getCardUid() == null || request.getCardUid().trim().isEmpty()) {
            return BoardingTapResponse.builder()
                    .status("INVALID")
                    .message("Card UID cannot be empty")
                    .build();
        }

        String rawUid = request.getCardUid().trim().toUpperCase();

        // 1. Input Sanitization (Hex check)
        if (!HEX_UID_PATTERN.matcher(rawUid).matches()) {
            log.warn("Invalid UID format rejected: {}", rawUid);
            return BoardingTapResponse.builder()
                    .status("INVALID")
                    .message("Malformed Card UID format: must be valid hexadecimal")
                    .cardUid(rawUid)
                    .build();
        }

        long now = System.currentTimeMillis();
        Long lastTap = lastTapTimes.get(rawUid);

        // 2. 5-Second Debounce Protection
        if (lastTap != null && (now - lastTap) < DEBOUNCE_COOLDOWN_MS) {
            long remaining = DEBOUNCE_COOLDOWN_MS - (now - lastTap);
            log.info("Card {} tapped within cooldown. Debouncing ({} ms remaining)", rawUid, remaining);
            return BoardingTapResponse.builder()
                    .status("DEBOUNCED")
                    .message("Card already processed. Please step aboard.")
                    .cardUid(rawUid)
                    .cooldownRemainingMs(remaining)
                    .build();
        }

        lastTapTimes.put(rawUid, now);

        Long vehicleId = request.getVehicleId() != null ? request.getVehicleId() : 1L;
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);

        // 3. Match Nearest Station via GPS (if provided)
        Station nearestStation = null;
        if (request.getLatitude() != null && request.getLongitude() != null) {
            nearestStation = findNearestStation(request.getLatitude(), request.getLongitude());
        }

        // 4. Verify Registered Student Card
        Optional<StudentCard> cardOpt = studentCardRepository.findByCardUidIgnoreCase(rawUid);
        if (cardOpt.isEmpty()) {
            log.warn("Unregistered Card Scanned: {}", rawUid);
            saveBoardingLog(rawUid, null, vehicle, nearestStation, "UNREGISTERED");

            return BoardingTapResponse.builder()
                    .status("UNREGISTERED")
                    .message("Unregistered Card: Please register with MMU Admin")
                    .cardUid(rawUid)
                    .vehicleId(vehicleId)
                    .detectedStation(nearestStation != null ? nearestStation.getName() : "Unknown Stop")
                    .build();
        }

        StudentCard student = cardOpt.get();

        // 5. Compute Occupancy & Increment
        AtomicInteger currentOccupancy = vehicleOccupancyMap.computeIfAbsent(vehicleId, k -> new AtomicInteger(0));
        int newCount = currentOccupancy.incrementAndGet();

        // Save confirmed transaction
        saveBoardingLog(rawUid, student.getMatrixId(), vehicle, nearestStation, "CONFIRMED");

        // 6. Push Live WebSocket Broadcast
        broadcastOccupancy(vehicleId, newCount);

        log.info("Passenger Boarded: Student {} ({}) on Vehicle {}. Current Occupancy: {}",
                student.getStudentName(), student.getMatrixId(), vehicleId, newCount);

        return BoardingTapResponse.builder()
                .status("SUCCESS")
                .message("Welcome aboard, " + student.getStudentName())
                .cardUid(rawUid)
                .studentMatrixId(student.getMatrixId())
                .studentName(student.getStudentName())
                .faculty(student.getFaculty())
                .vehicleId(vehicleId)
                .detectedStation(nearestStation != null ? nearestStation.getName() : "Campus Station")
                .currentOccupancy(newCount)
                .maxCapacity(DEFAULT_MAX_CAPACITY)
                .build();
    }

    /**
     * Get current passenger count for vehicle
     */
    public int getVehicleOccupancy(Long vehicleId) {
        return vehicleOccupancyMap.computeIfAbsent(vehicleId, k -> new AtomicInteger(0)).get();
    }

    /**
     * Driver manually resets passenger count when route finishes
     */
    public int resetVehicleOccupancy(Long vehicleId) {
        AtomicInteger occ = vehicleOccupancyMap.computeIfAbsent(vehicleId, k -> new AtomicInteger(0));
        occ.set(0);
        broadcastOccupancy(vehicleId, 0);
        return 0;
    }

    private void saveBoardingLog(String cardUid, String matrixId, Vehicle vehicle, Station station, String status) {
        if (vehicle == null) {
            return;
        }
        try {
            ShuttleBoardingLog logEntry = new ShuttleBoardingLog();
            logEntry.setCardUid(cardUid);
            logEntry.setMatrixId(matrixId);
            logEntry.setVehicle(vehicle);
            logEntry.setStation(station);
            logEntry.setStatus(status);
            logEntry.setBoardingTime(LocalDateTime.now());
            boardingLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save boarding log: {}", e.getMessage());
        }
    }

    private void broadcastOccupancy(Long vehicleId, int occupancy) {
        if (messagingTemplate != null) {
            try {
                messagingTemplate.convertAndSend("/occupancy/" + vehicleId, occupancy);
                messagingTemplate.convertAndSend("/routes/occupancy", occupancy);
            } catch (Exception e) {
                log.warn("WebSocket broadcast failed: {}", e.getMessage());
            }
        }
    }

    private Station findNearestStation(double lat, double lng) {
        List<Station> stations = stationRepository.findAll();
        Station closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Station s : stations) {
            double d = calculateHaversineDistance(lat, lng, s.getLatitude(), s.getLongitude());
            if (d < minDistance) {
                minDistance = d;
                closest = s;
            }
        }
        return (minDistance <= 0.5) ? closest : null; // Within 500 meters
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}


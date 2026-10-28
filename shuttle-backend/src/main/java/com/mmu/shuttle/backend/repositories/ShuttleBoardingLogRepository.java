package com.mmu.shuttle.backend.repositories;

import com.mmu.shuttle.backend.entities.ShuttleBoardingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShuttleBoardingLogRepository extends JpaRepository<ShuttleBoardingLog, Long> {

    List<ShuttleBoardingLog> findByVehicleIdOrderByBoardingTimeDesc(Long vehicleId);

    @Query("SELECT COUNT(b) FROM ShuttleBoardingLog b WHERE b.vehicle.id = :vehicleId AND b.boardingTime >= :sinceTime AND b.status = 'CONFIRMED'")
    long countConfirmedBoardingsSince(@Param("vehicleId") Long vehicleId, @Param("sinceTime") LocalDateTime sinceTime);
}


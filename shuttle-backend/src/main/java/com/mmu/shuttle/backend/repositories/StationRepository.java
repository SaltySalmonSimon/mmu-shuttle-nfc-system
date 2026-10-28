package com.mmu.shuttle.backend.repositories;

import com.mmu.shuttle.backend.entities.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
}


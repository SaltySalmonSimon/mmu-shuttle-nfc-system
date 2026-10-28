package com.mmu.shuttle.backend.repositories;

import com.mmu.shuttle.backend.entities.StudentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentCardRepository extends JpaRepository<StudentCard, String> {
    Optional<StudentCard> findByCardUidIgnoreCase(String cardUid);
    Optional<StudentCard> findByMatrixId(String matrixId);
}


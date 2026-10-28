package com.mmu.shuttle.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentCard {

    @Id
    @Column(name = "card_uid", length = 32, nullable = false)
    private String cardUid;

    @Column(name = "matrix_id", length = 20, nullable = false, unique = true)
    private String matrixId;

    @Column(name = "student_name", length = 100, nullable = false)
    private String studentName;

    @Column(name = "faculty", length = 50)
    private String faculty;

    @Column(name = "status", length = 20)
    private String status; // ACTIVE, SUSPENDED, LOST

    @Column(name = "registered_at")
    private LocalDateTime registeredAt = LocalDateTime.now();
}


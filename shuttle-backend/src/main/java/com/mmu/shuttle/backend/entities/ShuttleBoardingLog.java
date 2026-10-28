package com.mmu.shuttle.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shuttle_boarding_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShuttleBoardingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_uid", length = 32, nullable = false)
    private String cardUid;

    @Column(name = "matrix_id", length = 20)
    private String matrixId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;

    @Column(name = "boarding_time")
    private LocalDateTime boardingTime = LocalDateTime.now();

    @Column(name = "status", length = 20)
    private String status; // CONFIRMED, UNREGISTERED, DEBOUNCED
}


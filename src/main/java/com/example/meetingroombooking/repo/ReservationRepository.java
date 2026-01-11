package com.example.meetingroombooking.repo;

import com.example.meetingroombooking.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}

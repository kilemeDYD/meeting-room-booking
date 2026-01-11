package com.example.meetingroombooking.repo;

import com.example.meetingroombooking.domain.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findByUserIdOrderByStartTimeDesc(Long userId, Pageable pageable);
}

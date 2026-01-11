package com.example.meetingroombooking.repo;

import com.example.meetingroombooking.domain.ReservationSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {
    void deleteByReservationId(Long reservationId);
}

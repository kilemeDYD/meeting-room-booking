package com.example.meetingroombooking.repo;

import com.example.meetingroombooking.domain.entity.ReservationSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {

    void deleteByReservationId(Long reservationId);

    List<ReservationSlot> findByRoomIdAndSlotStartBetweenOrderBySlotStartAsc(
            Long roomId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );
}

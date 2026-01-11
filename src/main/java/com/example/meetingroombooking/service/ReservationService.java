package com.example.meetingroombooking.service;

import com.example.meetingroombooking.domain.Reservation;
import com.example.meetingroombooking.domain.ReservationSlot;
import com.example.meetingroombooking.domain.ReservationStatus;
import com.example.meetingroombooking.repo.ReservationRepository;
import com.example.meetingroombooking.repo.ReservationSlotRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final ReservationSlotRepository slotRepo;

    public ReservationService(ReservationRepository reservationRepo, ReservationSlotRepository slotRepo) {
        this.reservationRepo = reservationRepo;
        this.slotRepo = slotRepo;
    }

    @Transactional
    public long create(long userId, long roomId, LocalDate date,
                       LocalTime start, LocalTime end, String purpose) {

        List<Integer> slots = TimeSlotUtil.coveredSlots(start, end);

        Reservation r = new Reservation();
        r.setUserId(userId);
        r.setRoomId(roomId);
        r.setDate(date);
        r.setStartTime(start);
        r.setEndTime(end);
        r.setPurpose(purpose);
        r.setStatus(ReservationStatus.APPROVED);

        r = reservationRepo.save(r);

        try {
            for (Integer slotIndex : slots) {
                ReservationSlot s = new ReservationSlot();
                s.setReservationId(r.getId());
                s.setRoomId(roomId);
                s.setDate(date);
                s.setSlotIndex(slotIndex);
                slotRepo.save(s);
            }
        } catch (DataIntegrityViolationException ex) {
            // 唯一键冲突：并发/重复占用
            throw new TimeConflictException("Time slot conflict for room " + roomId, ex);
        }

        return r.getId();
    }

    @Transactional
    public void cancel(long reservationId) {
        slotRepo.deleteByReservationId(reservationId);
        reservationRepo.deleteById(reservationId);
    }
}

package com.example.meetingroombooking.repo;

import com.example.meetingroombooking.domain.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {
    List<MeetingRoom> findByStatusOrderByIdAsc(String status);
}

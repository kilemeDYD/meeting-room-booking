package com.example.meetingroombooking.service;

import com.example.meetingroombooking.domain.entity.Reservation;
import com.example.meetingroombooking.domain.entity.ReservationSlot;
import com.example.meetingroombooking.domain.enums.ReservationStatus;
import com.example.meetingroombooking.repo.ReservationRepository;
import com.example.meetingroombooking.repo.ReservationSlotRepository;
import com.example.meetingroombooking.repo.RoomRepository;
import com.example.meetingroombooking.web.dto.ReservationCreateForm;
import com.example.meetingroombooking.web.exception.BusinessException;
import com.example.meetingroombooking.web.exception.ConflictException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    private final int slotMinutes;
    private final int bookableDaysAhead;
    private final int maxDurationMinutes;

    public ReservationService(
            RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            ReservationSlotRepository reservationSlotRepository,
            @Value("${app.slot-minutes:15}") int slotMinutes,
            @Value("${app.bookable-days-ahead:30}") int bookableDaysAhead,
            @Value("${app.max-duration-minutes:240}") int maxDurationMinutes
    ) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.reservationSlotRepository = reservationSlotRepository;
        this.slotMinutes = slotMinutes;
        this.bookableDaysAhead = bookableDaysAhead;
        this.maxDurationMinutes = maxDurationMinutes;
    }

    /**
     * 强一致创建预约：
     * 1) 校验
     * 2) 插入 reservation
     * 3) 批量插入 reservation_slot（uk_room_slot 冲突则回滚）
     */
    @Transactional
    public Long create(Long userId, ReservationCreateForm form) {
        if (userId == null) throw new BusinessException("未登录或 userId 为空");
        validateBasic(form);

        // 会议室存在性 + 是否启用
        var room = roomRepository.findById(form.getRoomId())
                .orElseThrow(() -> new BusinessException("会议室不存在"));
        if (Boolean.FALSE.equals(room.getEnabled())) {
            throw new BusinessException("会议室已停用");
        }

        // 插入 reservation 主表
        Reservation res = new Reservation();
        res.setRoomId(form.getRoomId());
        res.setUserId(userId);
        res.setTitle(form.getTitle().trim());
        res.setDescription(form.getDescription());
        res.setStartTime(form.getStartTime());
        res.setEndTime(form.getEndTime());
        res.setStatus(ReservationStatus.PENDING);

        Reservation saved = reservationRepository.saveAndFlush(res); // 先拿到 id

        // 计算 slots
        List<LocalDateTime> slots = computeSlots(form.getStartTime(), form.getEndTime(), slotMinutes);

        // 批量插入 slots：冲突由唯一键兜底
        try {
            List<ReservationSlot> slotEntities = new ArrayList<>(slots.size());
            for (LocalDateTime t : slots) {
                ReservationSlot s = new ReservationSlot();
                s.setReservationId(saved.getId());
                s.setRoomId(form.getRoomId());
                s.setSlotStart(t);
                slotEntities.add(s);
            }
            reservationSlotRepository.saveAllAndFlush(slotEntities);
        } catch (DataIntegrityViolationException e) {
            // 关键：唯一约束冲突 → 409
            // 事务会回滚：reservation + slots 都不落库
            throw new ConflictException("该会议室在所选时间段已被占用");
        }

        return saved.getId();
    }

    private void validateBasic(ReservationCreateForm form) {
        if (form == null) throw new BusinessException("参数为空");
        if (form.getRoomId() == null) throw new BusinessException("roomId 为空");
        if (form.getStartTime() == null || form.getEndTime() == null) throw new BusinessException("开始/结束时间为空");
        if (!form.getEndTime().isAfter(form.getStartTime())) throw new BusinessException("结束时间必须晚于开始时间");

        // 对齐时间片边界
        if (!isAligned(form.getStartTime(), slotMinutes) || !isAligned(form.getEndTime(), slotMinutes)) {
            throw new BusinessException("预约时间必须对齐时间片边界（" + slotMinutes + "分钟）");
        }

        long durationMin = Duration.between(form.getStartTime(), form.getEndTime()).toMinutes();
        if (durationMin <= 0) throw new BusinessException("预约时长非法");
        if (durationMin > maxDurationMinutes) {
            throw new BusinessException("预约时长超过上限：" + maxDurationMinutes + "分钟");
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = form.getStartTime().toLocalDate();
        if (startDate.isBefore(today)) throw new BusinessException("不能预约过去时间");
        if (startDate.isAfter(today.plusDays(bookableDaysAhead))) {
            throw new BusinessException("只能预约未来 " + bookableDaysAhead + " 天内");
        }

        if (form.getTitle() == null || form.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        }
    }

    private static boolean isAligned(LocalDateTime t, int slotMinutes) {
        return (t.getMinute() % slotMinutes == 0) && (t.getSecond() == 0) && (t.getNano() == 0);
    }

    /**
     * start inclusive, end exclusive: [start, end)
     * 例：10:00-10:45（15min）=> 10:00,10:15,10:30
     */
    private static List<LocalDateTime> computeSlots(LocalDateTime start, LocalDateTime end, int slotMinutes) {
        List<LocalDateTime> list = new ArrayList<>();
        LocalDateTime cur = start;
        while (cur.isBefore(end)) {
            list.add(cur);
            cur = cur.plusMinutes(slotMinutes);
        }
        return list;
    }
}

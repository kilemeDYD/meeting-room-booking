package com.example.meetingroombooking;

import com.example.meetingroombooking.service.ReservationService;
import com.example.meetingroombooking.service.TimeConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ReservationServiceTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private long getAdminUserId() {
        Long id = jdbcTemplate.queryForObject(
                "select id from user_account where username = ?",
                Long.class, "admin"
        );
        if (id == null) throw new IllegalStateException("admin user not found");
        return id;
    }

    private long getRoomIdA101() {
        Long id = jdbcTemplate.queryForObject(
                "select id from meeting_room where name = ?",
                Long.class, "A101"
        );
        if (id == null) throw new IllegalStateException("room A101 not found");
        return id;
    }

    @Test
    void shouldConflictOnSameSlot_andRollbackReservation() {
        long userId = getAdminUserId();
        long roomId = getRoomIdA101();

        // 用固定日期避免“今天/明天”导致数据难复现
        LocalDate date = LocalDate.of(2026, 1, 11);

        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(10, 0);

        // 让测试可重复运行：先清掉该房间该天的数据（只清相关范围）
        jdbcTemplate.update("delete from reservation_slot where room_id=? and date=?", roomId, date);
        jdbcTemplate.update("delete from reservation where room_id=? and date=? and start_time=? and end_time=?",
                roomId, date, start, end);

        // 第一次创建：应成功
        reservationService.create(userId, roomId, date, start, end, "test1");

        // 第二次同时间段：必须冲突
        assertThrows(TimeConflictException.class, () ->
                reservationService.create(userId, roomId, date, start, end, "test2")
        );

        // 核心验证：第二次 reservation 必须回滚，最终只有 1 条
        Integer cntReservation = jdbcTemplate.queryForObject(
                "select count(*) from reservation where room_id=? and date=? and start_time=? and end_time=?",
                Integer.class, roomId, date, start, end
        );
        assertEquals(1, cntReservation);

        //（可选）再验证 slot 只属于第一次创建（9:00-10:00 一共 4 个 15min 槽）
        Integer cntSlot = jdbcTemplate.queryForObject(
                "select count(*) from reservation_slot where room_id=? and date=?",
                Integer.class, roomId, date
        );
        assertEquals(4, cntSlot);
    }
}

package com.example.meetingroombooking;

import com.example.meetingroombooking.service.ReservationService;
import com.example.meetingroombooking.web.dto.ReservationCreateForm;
import com.example.meetingroombooking.web.exception.ConflictException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Test
    void concurrent_create_same_slot_only_one_success() throws Exception {
        Long userId = 2L;     // data.sql 里 user1 的 id 通常是 2（如果不是也没关系：后面可以改成查库）
        Long roomId = 1L;     // room 第一条通常是 1

        LocalDateTime start = LocalDate.now().plusDays(1).atTime(10, 0);
        LocalDateTime end = LocalDate.now().plusDays(1).atTime(11, 0);

        ReservationCreateForm form1 = new ReservationCreateForm();
        form1.setRoomId(roomId);
        form1.setStartTime(start);
        form1.setEndTime(end);
        form1.setTitle("并发测试-1");

        ReservationCreateForm form2 = new ReservationCreateForm();
        form2.setRoomId(roomId);
        form2.setStartTime(start);
        form2.setEndTime(end);
        form2.setTitle("并发测试-2");

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger conflict = new AtomicInteger(0);

        Runnable task1 = () -> {
            ready.countDown();
            try {
                go.await();
                reservationService.create(userId, form1);
                success.incrementAndGet();
            } catch (ConflictException e) {
                conflict.incrementAndGet();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        Runnable task2 = () -> {
            ready.countDown();
            try {
                go.await();
                reservationService.create(userId, form2);
                success.incrementAndGet();
            } catch (ConflictException e) {
                conflict.incrementAndGet();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        Thread t1 = new Thread(task1);
        Thread t2 = new Thread(task2);
        t1.start();
        t2.start();

        ready.await();
        go.countDown();
        t1.join();
        t2.join();

        Assertions.assertEquals(1, success.get(), "应该只成功 1 个");
        Assertions.assertEquals(1, conflict.get(), "应该只冲突 1 个");
    }
}

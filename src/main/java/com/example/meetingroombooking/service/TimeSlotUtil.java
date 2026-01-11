package com.example.meetingroombooking.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class TimeSlotUtil {
    private TimeSlotUtil() {}

    public static final int SLOT_MINUTES = 15;

    public static List<Integer> coveredSlots(LocalTime start, LocalTime end) {
        validateAligned(start);
        validateAligned(end);
        if (!end.isAfter(start)) throw new IllegalArgumentException("end must be after start");

        int startMinutes = start.getHour() * 60 + start.getMinute();
        int endMinutes = end.getHour() * 60 + end.getMinute();

        int startSlot = startMinutes / SLOT_MINUTES;
        int endSlotExclusive = endMinutes / SLOT_MINUTES;

        List<Integer> slots = new ArrayList<>(endSlotExclusive - startSlot);
        for (int s = startSlot; s < endSlotExclusive; s++) slots.add(s);
        return slots;
    }

    private static void validateAligned(LocalTime t) {
        if (t.getSecond() != 0 || t.getNano() != 0) {
            throw new IllegalArgumentException("time must have 0 second/nano");
        }
        int minutes = t.getHour() * 60 + t.getMinute();
        if (minutes % SLOT_MINUTES != 0) {
            throw new IllegalArgumentException("time must align to " + SLOT_MINUTES + " minutes");
        }
    }
}

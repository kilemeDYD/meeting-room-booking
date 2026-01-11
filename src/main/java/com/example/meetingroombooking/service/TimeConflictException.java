package com.example.meetingroombooking.service;

public class TimeConflictException extends RuntimeException {
    public TimeConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

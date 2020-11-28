package com.uber.uberapi.exceptions;

public class InvalidActionForBookingStateException extends BookingException {
    public InvalidActionForBookingStateException(String message) {
        super(message);
    }
}

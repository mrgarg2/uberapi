package com.uber.uberapi.exceptions;

public abstract class UberException extends RuntimeException {
    public UberException(String message) {
        super(message);
    }
}

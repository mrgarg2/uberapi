package com.uber.uberapi.exceptions;

public abstract class BookingException extends UberException{

    public BookingException(String message){
        super(message);
    }
}

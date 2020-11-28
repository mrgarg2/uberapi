package com.uber.uberapi.models;

import lombok.Getter;

@Getter
public enum BookingStatus {
    CANCELLED("The booking has been cancelled for some reason"),
    SCHEDULED("The booking is scheduled for sometime in future"),
    ASSIGNING_DRIVER("Passenger requested booking, driver yet to be assigned"),
    REACHING_PICKUP_LOACTION("THe driver has been assigned reaching the pickup point"),
    CAB_ARRIVED("THe driver has arrived at pickup point"),
    IN_RIDE("THe ride is currently in progress"),
    COMPLETED("THe ride has been completed already");


    private final String description;

    BookingStatus(String description){
        this.description = description;
    }
}

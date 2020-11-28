package com.uber.uberapi.services.drivermatching.filters;

import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.Gender;
import com.uber.uberapi.services.Constants;
import jdk.nashorn.internal.objects.annotations.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class GenderFilter extends DriverFilter {

    public GenderFilter(Constants constants){
        super(constants);
    }

    @Override
    public List<Driver> apply(List<Driver> drivers, Booking booking) {
        //male driver only male passenger
        Gender passengerGender = booking.getPassenger().getGender();
        drivers.stream()
                .filter(driver -> {
                    Gender driverGender = driver.getGender();
                    return !driverGender.equals(Gender.MALE) || passengerGender.equals(Gender.MALE);
                }).collect(Collectors.toList());
        return null;
    }
}

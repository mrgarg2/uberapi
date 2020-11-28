package com.uber.uberapi.services.drivermatching.filters;

import com.uber.uberapi.models.Booking;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.ExactLocation;
import com.uber.uberapi.services.Constants;
import com.uber.uberapi.services.ETAService;

import java.util.List;
import java.util.stream.Collectors;

public class VIPFilter extends DriverFilter{


    private final ETAService etaService;

    public VIPFilter(ETAService etaService, Constants constants){
        super(constants);
        this.etaService = etaService;
    }

    @Override
    public List<Driver> apply(List<Driver> drivers, Booking booking) {
        if(!getConstants().getIsETABasedFilterEnabled())    return drivers;
        ExactLocation pickup = booking.getPickupLocation();
        return drivers.stream().filter(driver -> {
            return etaService.getETAMinutes(driver.getLastKnownExactLocation(), pickup) <= getConstants().getMaxDriverETAMinutes();

        }).collect(Collectors.toList());
    }
}

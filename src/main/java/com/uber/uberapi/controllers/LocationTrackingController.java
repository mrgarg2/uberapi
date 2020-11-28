package com.uber.uberapi.controllers;

import com.uber.uberapi.exceptions.InvalidDriverException;
import com.uber.uberapi.exceptions.InvalidPassengerException;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.ExactLocation;
import com.uber.uberapi.models.Passenger;
import com.uber.uberapi.repositories.DriverRepository;
import com.uber.uberapi.repositories.PassengerRepository;
import com.uber.uberapi.services.Constants;
import com.uber.uberapi.services.locationtracking.LocationTrackingService;
import com.uber.uberapi.services.messagequeue.MessageQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/location")
public class LocationTrackingController {

    @Autowired
    DriverRepository driverRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    LocationTrackingService locationTrackingService;

    @Autowired
    MessageQueue messageQueue;

    @Autowired
    Constants constants;

    public Driver getDriverFromId(Long driverId){

        Optional<Driver> driver = driverRepository.findById(driverId);
        if(!driver.isPresent()){
            throw new InvalidDriverException("No Driver with id - " + driverId);
        }
        return driver.get();
    }

    public Passenger getPassengerFromId(Long passengerId){

        Optional<Passenger> passenger = passengerRepository.findById(passengerId);
        if(!passenger.isPresent()){
            throw new InvalidPassengerException("No Passenger with id - " + passengerId);
        }
        return passenger.get();
    }

    @PutMapping("/driver/{driverId}")
    public void updateDriverLocation(@PathVariable Long driverId,
                                     @RequestBody ExactLocation location){
        //once evry 3 sec for each active driver
        Driver driver = getDriverFromId(driverId);

        ExactLocation exactLocation = ExactLocation.builder()
                                        .longitude(location.getLongitude())
                                        .latitude(location.getLatitude())
                                        .build();

        messageQueue.sendMessage(constants.getLocationTrackingTopicName(), new LocationTrackingService.Message(driver, location));
        locationTrackingService.updateDriverLocation(driver, exactLocation);

    }

    @PutMapping("/passenger/{passengerId}")
    public void updatePassengerLocation(@PathVariable Long passengerId,
                                        @RequestBody ExactLocation location){
        //only trigger every 30 sec     if passenger active
        Passenger passenger = getPassengerFromId(passengerId);

        passenger.setLastKnownExactLocation(ExactLocation.builder()
                                                .longitude(location.getLongitude())
                                                .latitude(location.getLatitude())
                                                .build());

        passengerRepository.save(passenger);
    }
}

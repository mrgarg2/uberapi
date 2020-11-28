package com.uber.uberapi.controllers;

import com.uber.uberapi.exceptions.InvalidBookingException;
import com.uber.uberapi.exceptions.InvalidDriverException;
import com.uber.uberapi.models.*;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.DriverRepository;
import com.uber.uberapi.repositories.ReviewRepository;
import com.uber.uberapi.services.BookingService;
import com.uber.uberapi.services.Constants;
import com.uber.uberapi.services.drivermatching.DriverMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/driver")
public class DriverController {
    //all driver endpoints

    final DriverRepository driverRepository;

    final BookingRepository bookingRepository;

    final DriverMatchingService driverMatchingService;

    final ReviewRepository reviewRepository;

    final BookingService bookingService;

    final Constants constants;

    public DriverController(DriverRepository driverRepository, BookingRepository bookingRepository, DriverMatchingService driverMatchingService, ReviewRepository reviewRepository, BookingService bookingService, Constants constants) {
        this.driverRepository = driverRepository;
        this.bookingRepository = bookingRepository;
        this.driverMatchingService = driverMatchingService;
        this.reviewRepository = reviewRepository;
        this.bookingService = bookingService;
        this.constants = constants;

    }

    public Driver getDriverFromId(Long driverId){

        Optional<Driver> driver = driverRepository.findById(driverId);
        if(!driver.isPresent()){
            throw new InvalidDriverException("No Driver with id - " + driverId);
        }
        return driver.get();
    }

    public Booking getDriverBookingFromId(Long bookingId, Driver driver){

        Booking booking = getBookingFromId(bookingId);
        if(!booking.getDriver().equals(driver)){
            throw new InvalidBookingException("Driver " + driver.getId() + " has no such booking " + bookingId);
        }
        return booking;
    }

    private Booking getBookingFromId(Long bookingId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (!booking.isPresent()){
            throw new InvalidBookingException("No booking with id - " + bookingId);
        }

        return booking.get();
    }

    //session/jwt basd authentication
    @GetMapping("/{driverId}")
    public Driver getDriverDetails(@PathVariable(name = "driverId") Long driverId){
        //make sure driver auth & has same driver Id
        return getDriverFromId(driverId);
    }

    @PatchMapping("/{driverId}")
    public void changeAvailability(@PathVariable(name= "driverId") Long driverId,
                                   @RequestBody Boolean available){

        Driver driver = getDriverFromId(driverId);
        driver.setIsAvailable(available);
    }

    @GetMapping("/{driverId}/bookings")
    public List<Booking> getAllBookings(@PathVariable(name = "driverId") Long driverId){

        Driver driver = getDriverFromId(driverId);
        return driver.getBookings();
    }

    @GetMapping("/{driverId}/bookings/{bookingId}")
    public Booking getBooking(@PathVariable(name = "driverId") Long driverId,
                                    @PathVariable(name= "bookingId") Long bookingId){

        Driver driver = getDriverFromId(driverId);
        //driver can also see their own bookings
        return getDriverBookingFromId(bookingId, driver);

    }

    @PostMapping("/{driverId}/bookings/{bookingsId}")
    public void acceptBooking(@PathVariable(name= "driverId") Long driverId,
                              @PathVariable(name= "bookingId") Long bookingId){
        Driver driver = getDriverFromId(driverId);
        Booking booking = getBookingFromId(bookingId);

        bookingService.acceptBooking(driver, booking);

    }

    @DeleteMapping("/{driverId}/bookings/{bookingsId}")
    public void cancelBooking(@PathVariable(name= "driverId") Long driverId,
                              @PathVariable(name= "bookingId") Long bookingId){
        Driver driver = getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId, driver);
        bookingService.cancelByDriver(driver, booking);
        driverMatchingService.cancelByDriver(driver, booking);
    }


    @PatchMapping("/{driverId}/bookings/{bookingsId}/start")
    public void startRide(@PathVariable(name= "driverId") Long driverId,
                          @PathVariable(name= "bookingId") Long bookingId,
                          @RequestBody OTP otp){

        Driver driver = getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId, driver);

        booking.startRide(otp, constants.getRideStartOTPExpiryMinutes());
        bookingRepository.save(booking);
    }

    @PatchMapping("/{driverId}/bookings/{bookingsId}/end")
    public void endRide(@PathVariable(name= "driverId") Long driverId,
                          @PathVariable(name= "bookingId") Long bookingId){

        Driver driver = getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId, driver);

        booking.endRide();
        bookingRepository.save(booking);
        driverRepository.save(driver);
    }

    @PatchMapping("/{driverId}/bookings/{bookingsId}/rate")
    public void rateRide(@PathVariable(name= "driverId") Long driverId,
                        @PathVariable(name= "bookingId") Long bookingId,
                        @RequestBody Review data){

        Driver driver = getDriverFromId(driverId);
        Booking booking = getDriverBookingFromId(bookingId, driver);

        Review review = Review.builder()
                        .ratingOutOfFive(data.getRatingOutOfFive())
                        .note(data.getNote())
                .build();

        booking.setReviewByDriver(review);
        reviewRepository.save(review);
        bookingRepository.save(booking);
    }





}

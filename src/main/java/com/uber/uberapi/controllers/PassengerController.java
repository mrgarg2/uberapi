package com.uber.uberapi.controllers;

import com.uber.uberapi.exceptions.InvalidBookingException;
import com.uber.uberapi.exceptions.InvalidPassengerException;
import com.uber.uberapi.models.*;
import com.uber.uberapi.repositories.BookingRepository;
import com.uber.uberapi.repositories.PassengerRepository;
import com.uber.uberapi.repositories.ReviewRepository;
import com.uber.uberapi.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/passenger")
public class PassengerController {

    //all passenger endpoints

    final PassengerRepository passengerRepository;
    final BookingRepository bookingRepository;
    final ReviewRepository reviewRepository;
    final BookingService bookingService;

    public PassengerController(PassengerRepository passengerRepository, BookingRepository bookingRepository, ReviewRepository reviewRepository, BookingService bookingService) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.bookingService = bookingService;
    }

    public Passenger getPassengerFromId(Long passengerId){

        Optional<Passenger> passenger = passengerRepository.findById(passengerId);
        if(!passenger.isPresent()){
            throw new InvalidPassengerException("No Passenger with id - " + passengerId);
        }
        return passenger.get();
    }

    public Booking getPassengerBookingFromId(Long bookingId, Passenger passenger){

        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if(!optionalBooking.isPresent()){
            throw new InvalidBookingException("No booking with id - " + bookingId);
        }

        Booking booking = optionalBooking.get();
        if(!booking.getPassenger().equals(passenger)){
            throw new InvalidBookingException("Passenger " + passenger.getId() + " has no such booking " + bookingId);
        }
        return booking;
    }

    //session/jwt basd authentication
    @GetMapping("/{passengerId}")
    public Passenger getPassengerDetails(@PathVariable(name = "passengerId") Long passengerId){
        //make sure passenger auth & has same passenger Id

        Passenger passenger = getPassengerFromId(passengerId);

        return passenger;
    }

    @GetMapping("/{passengerId}/bookings")
    public List<Booking> getAllBookings(@PathVariable(name = "passengerId") Long passengerId){

        Passenger passenger = getPassengerFromId(passengerId);
        return passenger.getBookings();
    }

    @GetMapping("/{passengerId}/bookings/{bookingId}")
    public Booking getBooking(@PathVariable(name = "passengerId") Long passengerId,
                              @PathVariable(name= "bookingId") Long bookingId){

        Passenger passenger = getPassengerFromId(passengerId);
        return getPassengerBookingFromId(bookingId, passenger);

    }

    @PostMapping("/{passengerId}/bookings/")
    public void requestBooking(@PathVariable(name= "passengerId") Long passengerId,
                              @RequestBody Booking data){
        Passenger passenger = getPassengerFromId(passengerId);

        List<ExactLocation> route = new ArrayList<>();

        data.getRoute().forEach(location ->{
            route.add(ExactLocation.builder()
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build());
        });

        Booking booking = Booking.builder()
                            .rideStartOTP(OTP.make(passenger.getPhoneNumber()))
                            .route(route)
                            .passenger(passenger)
                            .bookingType(data.getBookingType())
                            .schdeuledTime(data.getSchdeuledTime())
                            .build();

        bookingService.createBooking(booking);

    }

    @PatchMapping("/{passengerId}/bookings/{bookingsId}")
    public void updateRoute(@PathVariable(name= "passengerId") Long passengerId,
                              @PathVariable(name= "bookingId") Long bookingId,
                            @RequestBody Booking data){
        Passenger passenger = getPassengerFromId(passengerId);
        Booking booking = getPassengerBookingFromId(bookingId, passenger);

        List<ExactLocation> route = new ArrayList<>(booking.getCompletedRoute());

        data.getRoute().forEach(location ->{
            route.add(ExactLocation.builder()
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build());
        });

        bookingService.updateRoute(booking, route);

    }

    @DeleteMapping("/{passengerId}/bookings/{bookingsId}")
    public void cancelBooking(@PathVariable(name= "passengerId") Long passengerId,
                              @PathVariable(name= "bookingId") Long bookingId){
        Passenger passenger = getPassengerFromId(passengerId);
        Booking booking = getPassengerBookingFromId(bookingId, passenger);

        bookingService.cancelByPassenger(passenger, booking);
    }


//    @PatchMapping("/{passengerId}/{bookingsId}/end")
//    public void endRide(@PathVariable(name= "passengerId") Long passengerId,
//                        @PathVariable(name= "bookingId") Long bookingId){
//
//        Passenger passenger = getPassengerFromId(passengerId);
//        Booking booking = getPassengerBookingFromId(bookingId, passenger);
//
//        booking.endRide();
//        bookingRepository.save(booking);
//    }

    @PatchMapping("/{passengerId}/bookings/{bookingsId}/rate")
    public void rateRide(@PathVariable(name= "passengerId") Long passengerId,
                        @PathVariable(name= "bookingId") Long bookingId,
                        @RequestBody Review data){

        Passenger passenger = getPassengerFromId(passengerId);
        Booking booking = getPassengerBookingFromId(bookingId, passenger);

        Review review = Review.builder()
                .ratingOutOfFive(data.getRatingOutOfFive())
                .note(data.getNote())
                .build();

        booking.setReviewByPassenger(review);
        reviewRepository.save(review);
        bookingRepository.save(booking);
    }

    @PostMapping("/{passengerId}/bookings/{bookingId}")
    public void retryBooking(@PathVariable(name= "passengerId") Long passengerId,
                             @PathVariable(name= "bookingId") Long bookingId){
        Passenger passenger = getPassengerFromId(passengerId);
        Booking booking = getPassengerBookingFromId(bookingId, passenger);

        bookingService.retryBooking(booking);
    }
}

package com.uber.uberapi.models;

import com.uber.uberapi.exceptions.InvalidActionForBookingStateException;
import com.uber.uberapi.exceptions.InvalidOTPException;
import lombok.*;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name="booking", indexes = {
        @Index(columnList = "passenger_id"),
        @Index(columnList = "driver_id")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Booking extends Auditable{

    @ManyToOne
    private Passenger passenger;

    @ManyToOne
    private Driver driver;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<Driver> notifiedDrivers = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    private BookingType bookingType;

    @OneToOne
    private Review reviewByPassenger;

    @OneToOne
    private Review reviewByDriver;

    @OneToOne
    private PaymentReceipt paymentReceipt;

    private BookingStatus bookingStatus;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "booking_route",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "exact_location_id"),
            indexes ={@Index(columnList = "booking_id")})
    @OrderColumn(name = "location_index")
    private List<ExactLocation> route = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "booking_completed_route",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "exact_location_id"),
            indexes ={@Index(columnList = "booking_id")})
    @OrderColumn(name = "location_index")
    private List<ExactLocation> completedRoute = new ArrayList<>();

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date schdeuledTime;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date startTime;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date endTime;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date expectedCompletionTime;

    @OneToOne
    private OTP rideStartOTP;

    private Long totalDistanceMeters;

    public void startRide(OTP otp, Integer rideStartOTPExpiryMinutes) {
        if(!bookingStatus.equals(BookingStatus.CAB_ARRIVED)){
            throw new InvalidActionForBookingStateException("Cannot start the ride before driver has reached pickup point");
        }

        if(!rideStartOTP.validateEnteredOTP(otp, rideStartOTPExpiryMinutes)){
            throw new InvalidOTPException();
        }

        startTime = new Date();
        passenger.setActiveBooking(this);
        bookingStatus = BookingStatus.IN_RIDE;
    }

    public void endRide() {
        if(!bookingStatus.equals(BookingStatus.IN_RIDE)){
            throw new InvalidActionForBookingStateException("RIde hasn't started yet");
        }
        driver.setActiveBooking(null);
        bookingStatus = BookingStatus.COMPLETED;
        endTime = new Date();
        passenger.setActiveBooking(null);
    }

    public boolean canChangeRoute() {
        return bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER)
                || bookingStatus.equals(BookingStatus.CAB_ARRIVED)
                || bookingStatus.equals(BookingStatus.IN_RIDE)
                || bookingStatus.equals(BookingStatus.SCHEDULED)
                || bookingStatus.equals(BookingStatus.REACHING_PICKUP_LOACTION);
    }

    public boolean needsDriver() {
        return bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER);
    }

    public ExactLocation getPickupLocation() {
        return route.get(0);
    }

    public void cancel() {
        if(!(bookingStatus.equals(BookingStatus.REACHING_PICKUP_LOACTION) ||
          bookingStatus.equals(BookingStatus.ASSIGNING_DRIVER) ||
                bookingStatus.equals(BookingStatus.SCHEDULED) ||
                bookingStatus.equals(BookingStatus.CAB_ARRIVED))){

            throw new InvalidActionForBookingStateException("Cannot cancel the ride now, if ride in progress ask driver to end ride");
        }

        bookingStatus = BookingStatus.CANCELLED;
        driver = null;
        notifiedDrivers.clear();

    }
}

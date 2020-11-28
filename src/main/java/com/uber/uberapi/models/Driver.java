package com.uber.uberapi.models;

import com.uber.uberapi.exceptions.UnapprovedDriverException;
import com.uber.uberapi.utils.DateUtils;
import lombok.*;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="driver")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Driver extends Auditable {

    private String pickUrl;

    @OneToOne
    private Account account;
    private Gender gender;
    private String name;

    @OneToOne
    private Review avgRating;

    @OneToOne(mappedBy = "driver")
    private Car car;

    private String licenceDetails;

    @Temporal(value = TemporalType.DATE)
    private Date dob;

    @Enumerated(value = EnumType.STRING)
    private DriverApprovalStatus approvalStatus;

    @OneToMany(mappedBy = "driver")
    private List<Booking> bookings;

    @ManyToMany(mappedBy = "notifiedDrivers", cascade = CascadeType.PERSIST)
    private Set<Booking> acceptableBookings = new HashSet<>();


    @OneToOne
    private Booking activeBooking = null;

    private Boolean isAvailable;

    private String activeCity;

    private String phoneNumber;

    @OneToOne
    private ExactLocation lastKnownExactLocation;

    @OneToOne
    private ExactLocation home;

    public void setAvailable(Boolean available) {

        if(available && !approvalStatus.equals(DriverApprovalStatus.APPROVED)) {
            throw new UnapprovedDriverException("Driver approval pending or denied " + getId());
        }

        isAvailable = available;
    }

    public boolean canAcceptBooking(int maxWaitTimeForPreviousRide) {
        if(isAvailable && activeBooking==null)  return true;

        return activeBooking.getExpectedCompletionTime().before(DateUtils.addMinutes(new Date(), maxWaitTimeForPreviousRide));

        //If current ride ends withing 10min.
    }
}

package com.uber.uberapi.services;

import com.uber.uberapi.models.ExactLocation;
import lombok.Setter;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ETAService {

    @Autowired
    Constants constants;

    public int getETAMinutes(ExactLocation lastKnownExactLocation, ExactLocation pickup) {
        return (int)(60.0 * lastKnownExactLocation.distanceKm(pickup) / constants.getDefaultETASpeedKmph());
    }
}

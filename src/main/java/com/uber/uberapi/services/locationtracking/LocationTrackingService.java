package com.uber.uberapi.services.locationtracking;

import com.uber.uberapi.exceptions.InvalidDriverException;
import com.uber.uberapi.models.Driver;
import com.uber.uberapi.models.ExactLocation;
import com.uber.uberapi.repositories.DriverRepository;
import com.uber.uberapi.services.Constants;
import com.uber.uberapi.services.messagequeue.MQMessage;
import com.uber.uberapi.services.messagequeue.MessageQueue;
import com.uber.uberapi.utils.quadtree.QuadTree;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationTrackingService {

    @Autowired
    MessageQueue messageQueue;

    @Autowired
    Constants constants;

    @Autowired
    DriverRepository driverRepository;

    @Autowired
    QuadTree world = new QuadTree();


    public List<Driver> getDriversNearLocation(ExactLocation pickup)  {
        List<Driver> collect = world.findNeighboursIds(pickup.getLatitude(),
                pickup.getLongitude(),
                constants.getMaxDistanceKmForDriverMatching())
                .stream()
                .map(driverId -> driverRepository.findById(driverId).orElseThrow(() -> new InvalidDriverException("No Such driver with id " + driverId)))
                .collect(Collectors.toList());
        return collect;
    }

    public void updateDriverLocation(Driver driver, ExactLocation location) {
        world.removeNeighbour(driver.getId());
        world.addNeighbour(driver.getId(), location.getLatitude(), location.getLongitude());
        driver.setLastKnownExactLocation(location);

    }

    @Scheduled(fixedRate = 1000)
    public void consumer(){
        MQMessage m = messageQueue.consumeMessage(constants.getDriverMatchingTopicName());
        if(m==null) return;

        Message message = (Message) m;
        updateDriverLocation(message.getDriver(), message.getLocation());

    }

    @Getter @Setter @AllArgsConstructor
    public static class Message implements MQMessage{
        private Driver driver;
        private ExactLocation location;
    }
}

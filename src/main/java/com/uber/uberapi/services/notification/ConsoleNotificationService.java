package com.uber.uberapi.services.notification;

import org.springframework.stereotype.Service;

@Service
public class ConsoleNotificationService implements NotificationService {
    @Override
    public void notify(String phoneNumber, String message) {
        System.out.println("Notification for " + phoneNumber + " : " + message);
    }
}

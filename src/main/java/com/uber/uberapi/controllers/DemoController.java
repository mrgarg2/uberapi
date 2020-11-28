package com.uber.uberapi.controllers;

import com.uber.uberapi.repositories.AccountRepository;
import com.uber.uberapi.repositories.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoController implements CommandLineRunner {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    DriverRepository driverRepository;

    @Override
    public void run(String... args) throws Exception {
        S3Manager s3Manager;
    }
}

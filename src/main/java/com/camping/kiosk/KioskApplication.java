package com.camping.kiosk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class KioskApplication {

    public static void main(String[] args) {
        SpringApplication.run(KioskApplication.class, args);
    }
}



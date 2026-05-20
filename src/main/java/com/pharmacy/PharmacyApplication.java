package com.pharmacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry point for the Pharmacy Admin application.
 * @EnableAsync enables asynchronous event listener processing
 * (required by the Observer pattern — AuditLogListener).
 */
@SpringBootApplication
@EnableAsync
public class PharmacyApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmacyApplication.class, args);
    }
}

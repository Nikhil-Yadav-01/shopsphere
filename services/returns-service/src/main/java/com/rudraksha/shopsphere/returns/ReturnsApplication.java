package com.rudraksha.shopsphere.returns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ReturnsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReturnsApplication.class, args);
    }
}

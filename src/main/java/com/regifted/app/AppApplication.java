package com.regifted.app;

import com.regifted.app.service.UserService; // import your service
import com.regifted.app.dto.RegisterRequest; // import your DTO

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner; // import CommandLineRunner
import org.springframework.context.annotation.Bean; // import @Bean

@SpringBootApplication
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }
}


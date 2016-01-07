package com.udl.tfg.sposapp;

import com.udl.tfg.sposapp.handlers.SessionEventHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SposAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SposAppApplication.class, args);
    }
}

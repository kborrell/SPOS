package com.udl.tfg.sposapp.configuration;

import com.udl.tfg.sposapp.handlers.SessionEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class HandlersConfiguration {

    @Bean
    SessionEventHandler sessionEventHandler(){
        return new SessionEventHandler();
    }

}

package com.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjConfig {
    @Bean
    Car createMercedes(){
        return new Car("W124", "Mercedes", 3.2);
    }

    @Bean
    Car createBMW(){
        return new Car("M5 F90", "BMW", 4.4);
    }


}

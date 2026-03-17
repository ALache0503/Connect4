package de.thbingen.connect4.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"de.thbingen.connect4"})
@EnableFeignClients(basePackages = {"de.thbingen.connect4.common.ports.out"})
public class Connect4SecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(Connect4SecurityApplication.class, args);
    }

}

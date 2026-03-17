package de.thbingen.connect4.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"de.thbingen.connect4"})
@SpringBootApplication
public class Connect4GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(Connect4GatewayApplication.class, args);
    }

}

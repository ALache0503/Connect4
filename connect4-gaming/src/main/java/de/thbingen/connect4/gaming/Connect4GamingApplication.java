package de.thbingen.connect4.gaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"de.thbingen.connect4"})
@EnableScheduling
@EnableFeignClients(basePackages = {
        "de.thbingen.connect4.common.ports.out",
        "de.thbingen.connect4.gaming.ports.out"
})
public class Connect4GamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(Connect4GamingApplication.class, args);
    }

}

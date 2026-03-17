package de.thbingen.connect4.statistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"de.thbingen.connect4"})
@EnableFeignClients(basePackages = {
        "de.thbingen.connect4.common.ports.out"
})
@SpringBootApplication
public class Connect4StatisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(Connect4StatisticsApplication.class, args);
    }

}

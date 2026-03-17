package de.thbingen.connect4.friends;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"de.thbingen.connect4"})
@EntityScan(basePackages = {
        "de.thbingen.connect4.common.model",
        "de.thbingen.connect4.friends.model.entity"
})
@EnableJpaRepositories(basePackages = {"de.thbingen.connect4.friends.adapters.out"})
@EnableFeignClients(basePackages = {"de.thbingen.connect4.common.ports.out"})
public class Connect4FriendsApplication {
    public static void main(String[] args) {
        SpringApplication.run(Connect4FriendsApplication.class, args);
    }
}

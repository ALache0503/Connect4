package de.thbingen.connect4.security.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("securityExchange");
    }
}

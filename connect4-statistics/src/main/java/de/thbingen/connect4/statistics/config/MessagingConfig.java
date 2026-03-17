package de.thbingen.connect4.statistics.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
    @Bean
    public Queue gameEventsQueue() {
        return new Queue("gameEvents");
    }

    @Bean
    public Queue userEventsQueue() {
        return new Queue("userEvents");
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("gameExchange");
    }

    @Bean
    public TopicExchange securityExchange() {
        return new TopicExchange("securityExchange");
    }

    @Bean
    public Binding gameBinding(TopicExchange exchange, Queue gameEventsQueue) {
        return BindingBuilder.bind(gameEventsQueue)
                .to(exchange)
                .with("game.*"); // Routing key pattern
    }

    @Bean
    public Binding userBinding(TopicExchange securityExchange, Queue userEventsQueue) {
        return BindingBuilder.bind(userEventsQueue)
                .to(securityExchange)
                .with("user.*");
    }
}

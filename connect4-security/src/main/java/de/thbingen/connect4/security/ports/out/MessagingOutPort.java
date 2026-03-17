package de.thbingen.connect4.security.ports.out;

public interface MessagingOutPort {
    void sendUserCreatedEvent(Long userId);
}

package de.thbingen.connect4.common.model.dto;

import java.sql.Timestamp;

public record UserDTO(Long id,
                      String username,
                      Timestamp updatedAt,
                      Timestamp createdAt) {
}

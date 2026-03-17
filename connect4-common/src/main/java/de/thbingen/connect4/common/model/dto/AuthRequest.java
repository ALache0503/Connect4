package de.thbingen.connect4.common.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRequest {
    @NotBlank(message = "Username ist Pflicht")
    @Size(max = 32)
    private String username;

    @NotBlank(message = "Password ist Pflicht")
    @Size(max = 64)
    private String password;
}

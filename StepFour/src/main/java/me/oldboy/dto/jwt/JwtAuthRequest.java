package me.oldboy.dto.jwt;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing authentication information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtAuthRequest {

    /**
     * Auth user login
     */
    @NotEmpty
    @Size(min = 3, max = 64, message = "Wrong format (to short/to long)")
    private String login;

    /**
     * Auth user password
     */
    @NotEmpty
    @Size(min = 3, max = 128, message = "Wrong format (to short/to long)")
    private String password;
}

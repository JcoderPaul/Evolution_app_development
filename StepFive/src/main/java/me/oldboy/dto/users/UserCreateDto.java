package me.oldboy.dto.users;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * A record representing user create info.
 */
@Builder
public record UserCreateDto(@NotEmpty(message = "The login can not be EMPTY")
                            @Size(min = 3, max = 64, message = "Wrong format (to short/to long)")
                            String login,
                            @NotEmpty (message = "Password can not be EMPTY")
                            @Size(min = 3, max = 64, message = "Wrong format (to short/to long)")
                            String password,
                            @NotEmpty (message = "The user role can not be EMPTY")
                            @Pattern(regexp = "ADMIN|USER", message = "The user role can only be (ADMIN or USER)")
                            String role) {
}

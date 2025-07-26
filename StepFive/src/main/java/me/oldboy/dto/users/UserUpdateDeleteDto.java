package me.oldboy.dto.users;

import jakarta.validation.constraints.*;
import lombok.Builder;

/* Никакой кириллицы в message */
@Builder
public record UserUpdateDeleteDto(@NotNull
                                  @Positive(message = "User ID must be positive")
                                  Long userId,

                                  @NotEmpty
                                  @Size(min = 3, max = 64, message = "Wrong format (to short/to long)")
                                  String login,

                                  @NotEmpty
                                  @Size(min = 3, max = 64, message = "Wrong format (to short/to long)")
                                  String password,

                                  @NotEmpty(message = "The user role can not be EMPTY")
                                  @Pattern(regexp = "ADMIN|USER", message = "The user role can only be (ADMIN or USER)")
                                  String role) {
}

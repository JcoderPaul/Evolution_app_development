package me.oldboy.core.dto.users;

import javax.validation.constraints.*;

public record UserUpdateDeleteDto (@NotNull
                                   @Positive (message = "User ID must be positive. ID пользователя не может быть отрицательным")
                                   Long userId,

                                   @NotEmpty
                                   @Size(min = 3, max = 64, message = "Wrong format (to short/to long)")
                                   String userName,

                                   @NotEmpty
                                   @Size(min = 3, max = 64, message = "Wrong format (to short/to long)")
                                   String password,

                                   @NotNull(message = "The user role can not be NULL")
                                   @NotEmpty (message = "The user role can not be EMPTY")
                                   @Pattern(regexp = "ADMIN|USER", message = "The user role can only be (ADMIN or USER)")
                                   String role){
}
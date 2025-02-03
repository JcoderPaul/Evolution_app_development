package me.oldboy.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {
        @Size(min = 2, max = 64, message = "Field size value cannot be between 2 and 64")
        @NotBlank(message = "Field name value cannot be empty/null")
        private String userName;

        @Size(min = 2, max = 64, message = "Field size value cannot be between 2 and 64")
        @NotBlank(message = "Field password value cannot be empty/null")
        private String password;

        @NotBlank(message = "Field Role cannot de Blank")
        @Pattern(regexp = "ADMIN|USER", message = "Field value can only be (ADMIN or USER)")
        private String role;
}

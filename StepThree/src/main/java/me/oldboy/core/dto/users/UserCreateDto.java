package me.oldboy.core.dto.users;

import javax.validation.constraints.*;

/*
При быстрой валидации с использованием средств javax.validation.constraints
нужно учитывать особенности использования той или иной аннотации и то над
какими полями (string, integer, enum) их можно/нельзя использовать, см.
https://docs.oracle.com/javaee/7/api/javax/validation/constraints/package-summary.html
https://docs.jboss.org/hibernate/beanvalidation/spec/2.0/api/javax/validation/constraints/package-summary.html
https://docs.oracle.com/javaee/6/api/javax/validation/constraints/package-summary.html
*/
public record UserCreateDto(@NotEmpty (message = "The userName can not be EMPTY")
                            @Size(min = 3, max = 64, message = "Wrong format (to short/to long)")
                            String userName,
                            @NotEmpty (message = "Password can not be EMPTY")
                            @Size(min = 3, max = 64, message = "Wrong format (to short/to long)")
                            String password,
                            @NotNull(message = "The user role can not be NULL")
                            @NotEmpty (message = "The user role can not be EMPTY")
                            @Pattern(regexp = "ADMIN|USER", message = "The user role can only be (ADMIN or USER)")
                            String role) {
}

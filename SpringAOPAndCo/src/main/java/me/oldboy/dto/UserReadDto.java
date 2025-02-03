package me.oldboy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import me.oldboy.entity.options.Role;

@Builder
@Schema(description = "Возвращаемые 'открытые' данные сущности User без пароля")
public record UserReadDto (Long userId,
                           String userName,
                           Role role) {
}

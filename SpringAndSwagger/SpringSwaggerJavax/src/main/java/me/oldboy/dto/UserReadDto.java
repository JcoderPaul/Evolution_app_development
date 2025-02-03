package me.oldboy.dto;

import lombok.Builder;
import me.oldboy.entity.options.Role;

@Builder
public record UserReadDto (Long userId,
                           String userName,
                           Role role) {
}

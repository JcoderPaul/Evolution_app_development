package me.oldboy.dto.users;

import lombok.Builder;
import me.oldboy.models.entity.options.Role;

@Builder
public record UserReadDto (Long userId,
                           String login,
                           Role role){
}

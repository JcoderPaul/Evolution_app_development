package me.oldboy.dto.users;

import lombok.Builder;
import me.oldboy.models.entity.options.Role;

/**
 * A record representing user read info.
 */
@Builder
public record UserReadDto (Long userId,
                           String login,
                           Role role){
}

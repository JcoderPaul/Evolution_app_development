package me.oldboy.dto.users;

import me.oldboy.models.entity.options.Role;

/**
 * A record representing user read info.
 */
public record UserReadDto (Long userId,
                           String login,
                           Role role){
}

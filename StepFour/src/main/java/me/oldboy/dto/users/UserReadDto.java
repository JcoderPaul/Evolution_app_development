package me.oldboy.dto.users;

import me.oldboy.models.entity.options.Role;

public record UserReadDto (Long userId,
                           String login,
                           Role role){
}

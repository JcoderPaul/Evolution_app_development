package me.oldboy.core.dto.users;

import me.oldboy.core.model.database.entity.options.Role;

public record UserReadDto (Long userId,
                           String userName,
                           Role role){
}

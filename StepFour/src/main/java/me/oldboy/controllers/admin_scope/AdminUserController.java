package me.oldboy.controllers.admin_scope;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.annotations.Auditable;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.dto.users.UserUpdateDeleteDto;
import me.oldboy.exception.user_exception.UserControllerException;
import me.oldboy.models.audit.operations.AuditOperationType;
import me.oldboy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private final UserService userService;

    @Auditable(operationType = AuditOperationType.DELETE_USER)
    @PostMapping("/delete")
    public ResponseEntity<?> deleteUser(@Validated
                                        @RequestBody
                                        UserUpdateDeleteDto deleteDto) {

        Optional<UserReadDto> userForRemove = userService.findByLogin(deleteDto.login());

        if (userForRemove.isEmpty()) {
            throw new UserControllerException("Пользователь для удаления не найден!");
        }
        if (userForRemove.get().userId() != deleteDto.userId()) {
            throw new UserControllerException("Вы уверенны что хотите удалить именно этого пользователя?");
        }

        if (userService.delete(userForRemove.get().userId())) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Пользователь удален!");
        } else {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("Пользователь не удален!");
        }
    }

    @Auditable(operationType = AuditOperationType.UPDATE_USER)
    @PostMapping("/update")
    public boolean updateUser(@Validated
                              @RequestBody
                              UserUpdateDeleteDto updateDto) {
        return userService.update(updateDto);
    }

    @GetMapping("/all")
    public List<UserReadDto> getAllUser() {
        return userService.findAll();
    }
}

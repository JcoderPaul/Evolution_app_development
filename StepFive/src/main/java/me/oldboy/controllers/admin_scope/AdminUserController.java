package me.oldboy.controllers.admin_scope;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.auditor.core.annotation.Auditable;
import me.oldboy.auditor.core.entity.operations.AuditOperationType;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.dto.users.UserUpdateDeleteDto;
import me.oldboy.exception.user_exception.UserControllerException;
import me.oldboy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Auditable(operationType = AuditOperationType.DELETE_USER)
    @DeleteMapping("/delete")
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
            return ResponseEntity.ok().body("{\"message\": \"User removed!\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\": \"Remove failed!\"}");
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
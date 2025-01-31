package me.oldboy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.dto.UserReadDto;
import me.oldboy.entity.User;
import me.oldboy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* Краткий пример применения аннотаций SpringDoc-OpenApi */
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("cw_app")
@Tag(name = "User Controller", description = "Основной API класс управляющий сущностью User")
public class UserRestController {

    @Autowired
    private UserService userService;

    @GetMapping("users/{userId}")
    @Operation(summary = "Get user data by ID",
               description = "Получение открытых данных пользователя без пароля по его ID",
               tags = {"GET"})
    public ResponseEntity<UserReadDto> getUserById(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    /* В теле запроса передаем данные User и валидируем их */
    @PostMapping("users")
    @Operation(summary = "Save user data to BataBase",
               description = "Сохранение данных пользователя в БД",
               tags = {"SAVE"})
    public ResponseEntity<String> saveUser(@RequestBody
                                           @Validated
                                           User user) throws JsonProcessingException {
        if(userService.isUserExist(user.getUserName())){
            return ResponseEntity.badRequest().body("Attempt to duplicate data!");
        } else {
            /* Вернем наш UserReadDto в симпатишном строковом JSON формате */
            return ResponseEntity.ok(new ObjectMapper().writer()
                                                       .withDefaultPrettyPrinter()
                                                       .writeValueAsString(userService.createUser(user)));
        }
    }

    @GetMapping("users")
    @Operation(summary = "Get list of all users",
               description = "Получение открытых данных всех пользователей из БД списком",
               tags = {"GET"})
    public ResponseEntity<List<UserReadDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("users/{userId}")
    @Operation(summary = "Delete user data by ID",
               description = "Удаление данных пользователя по его ID",
               tags = {"DELETE/REMOVE"})
    public ResponseEntity<String> deleteUser(@PathVariable("userId") Long userId) {
        if(userService.deleteUser(userId)){
            return ResponseEntity.ok("User with ID " + userId + " deleted!");
        } else {
            return ResponseEntity.badRequest().body("User with ID " + userId + " not found!");
        }
    }
}
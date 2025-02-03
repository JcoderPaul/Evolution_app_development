package me.oldboy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.annotations.Measurable;
import me.oldboy.dto.UserCreateDto;
import me.oldboy.dto.UserReadDto;
import me.oldboy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("cw_app")
/* Краткий пример применения аннотаций SpringDoc-OpenApi */
@Tag(name = "User Controller", description = "Основной API класс управляющий сущностью User")
public class UserRestController {

    @Autowired
    private UserService userService;

    @GetMapping("users/{userId}")
    @Operation(summary = "Get user data by ID",
               description = "Получение открытых данных пользователя без пароля по его ID",
               tags = {"GET"})
    @Measurable
    public ResponseEntity<UserReadDto> getUserById(@PathVariable("userId") Long userId) {
        return userService.getUser(userId)
                .map(content -> ResponseEntity.ok().body(content))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /* В теле запроса передаем данные User и валидируем их */
    @PostMapping("users")
    @Operation(summary = "Save user data to BataBase",
               description = "Сохранение данных пользователя в БД",
               tags = {"SAVE"})
    @Measurable
    public ResponseEntity<String> saveUser(@Valid @RequestBody UserCreateDto createUser,
                                           BindingResult bindingResult) throws JsonProcessingException {
        if (bindingResult.hasErrors()){
            List<String> errorsMessage = bindingResult.getAllErrors().stream()
                                                                     .map(errors -> errors.getDefaultMessage())
                                                                     .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(new ObjectMapper().writer()
                                                                      .withDefaultPrettyPrinter()
                                                                      .writeValueAsString(errorsMessage));
        }
        if(userService.isUserExist(createUser.getUserName())){
            return ResponseEntity.badRequest().body("Attempt to duplicate data!");
        } else {
            /* Вернем наш UserReadDto в симпатишном строковом JSON формате */
            return ResponseEntity.ok(new ObjectMapper().writer()
                                                       .withDefaultPrettyPrinter()
                                                       .writeValueAsString(userService.createUser(createUser)));
        }
    }

    @GetMapping("users")
    @Operation(summary = "Get list of all users",
               description = "Получение открытых данных всех пользователей из БД списком",
               tags = {"GET"})
    @Measurable
    public ResponseEntity<List<UserReadDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("users/{userId}")
    @Operation(summary = "Delete user data by ID",
               description = "Удаление данных пользователя по его ID",
               tags = {"DELETE/REMOVE"})
    @Measurable
    public ResponseEntity<String> deleteUser(@PathVariable("userId") Long userId) {
        if(userService.deleteUser(userId)){
            return ResponseEntity.ok("User with ID " + userId + " deleted!");
        } else {
            return ResponseEntity.badRequest().body("User with ID " + userId + " not found!");
        }
    }
}
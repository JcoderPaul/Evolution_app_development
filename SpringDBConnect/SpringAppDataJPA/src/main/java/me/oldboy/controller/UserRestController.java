package me.oldboy.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.dto.UserReadDto;
import me.oldboy.entity.User;
import me.oldboy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* Если заглянуть в данную аннотацию, то можно заметить, что она интегрирует в себя, так же @Controller и @ResponseBody */
@RestController
@AllArgsConstructor
@NoArgsConstructor
/* Задаем общий префикс для всех запросов нашего условного API */
@RequestMapping("/v2")
public class UserRestController {

    @Autowired
    private UserService userService;

    @GetMapping("/users/{userId}")
    public String getUserName(@PathVariable("userId") Long userId) {
        String userName = userService.getUser(userId).userName();
        return "USER NAME: " + userName;
    }

    @PostMapping("/users")
    public String saveUser(@RequestBody User user) throws JsonProcessingException {
        if(userService.isUserExist(user.getUserName())){
            return "Attempt to duplicate data!";
        } else {
            /* Вернем наш UserReadDto в симпатишном строковом JSON формате */
            return new ObjectMapper().writer()
                                     .withDefaultPrettyPrinter()
                                     .writeValueAsString(userService.createUser(user));
        }
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/users/{userId}")
    public String deleteUser(@PathVariable("userId") Long userId) {
        if(userService.deleteUser(userId)){
            return "User with ID " + userId + " deleted!";
        } else {
            return "User with ID " + userId + " not found!";
        }
    }
}
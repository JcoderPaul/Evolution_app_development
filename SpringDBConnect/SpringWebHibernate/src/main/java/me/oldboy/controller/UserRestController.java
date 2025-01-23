package me.oldboy.controller;

import me.oldboy.entity.User;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import me.oldboy.service.UserService;

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
        String userName = userService.getUser(userId).getUserName();
        return "USER NAME: " + userName;
    }

    @PostMapping("/users")
    public User saveUser(@RequestBody User user) {
        return userService.createUser(user);
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

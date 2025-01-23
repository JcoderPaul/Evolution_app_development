package me.oldboy.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.entity.User;
import me.oldboy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@NoArgsConstructor
/*
Если заглянуть в данную аннотацию, то можно заметить, что
она интегрирует в себя, так же @Controller и @ResponseBody
*/
@RestController
public class UserRestController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/v2/users/{userId}",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public String getUserName(@PathVariable("userId") Long userId) {

        String userName = userService.getUser(userId).getUserName();

        return "USER NAME: " + userName;
    }

    @RequestMapping(value = "/v2/users",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public User saveUser(@RequestBody User user) {

        User createdUser = userService.createUser(user);

        return createdUser;
    }
}

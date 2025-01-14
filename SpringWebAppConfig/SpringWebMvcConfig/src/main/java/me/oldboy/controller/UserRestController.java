package me.oldboy.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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

    @RequestMapping(value = "/v3/users",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public String getUserName(@RequestParam("userId") Long userId) {

        String userName = userService.getUser(userId).getName();

        return "USER NAME: " + userName;
    }
}

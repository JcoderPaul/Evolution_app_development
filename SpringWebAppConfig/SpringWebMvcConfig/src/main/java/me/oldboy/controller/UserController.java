package me.oldboy.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@NoArgsConstructor
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/v1/users",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getUserName(@RequestParam(name = "userId") Long userId) {

        String userName = userService.getUser(userId).getName();

        return "USER NAME: " + userName;
    }

    @GetMapping(value = "/v2/users")
    public String getUserNameTwo(@RequestParam(name = "userId") Long userId, Model model) {

        String userName = "USER NAME: " + userService.getUser(userId).getName();
        model.addAttribute("printUserName", userName);

        return "/jsp/user_name.jsp";
    }
}

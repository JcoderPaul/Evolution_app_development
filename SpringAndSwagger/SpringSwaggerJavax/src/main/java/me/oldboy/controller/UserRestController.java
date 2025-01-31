package me.oldboy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.dto.UserReadDto;
import me.oldboy.entity.User;
import me.oldboy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("cw_app")
@Api(value = "User Controller", description = "User API")
public class UserRestController {

    @Autowired
    private UserService userService;

    @GetMapping("users/{userId}")
    @ApiOperation(value = "Get user by ID", response = UserReadDto.class)
    public ResponseEntity<UserReadDto> getUserById(@ApiParam(value = "The user ID we want to get from the database", required = true)
                                                   @PathVariable("userId")
                                                   Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PostMapping("users")
    @ApiOperation(value = "Save the received user in database", response = String.class)
    public ResponseEntity<String> saveUser(@RequestBody User user) throws JsonProcessingException {
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
    @ApiOperation(value = "Get all users from database", response = List.class)
    public ResponseEntity<List<UserReadDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("users/{userId}")
    @ApiOperation(value = "Delete user by ID", response = String.class)
    public ResponseEntity<String> deleteUser(@PathVariable("userId") Long userId) {
        if(userService.deleteUser(userId)){
            return ResponseEntity.ok("User with ID " + userId + " deleted!");
        } else {
            return ResponseEntity.badRequest().body("User with ID " + userId + " not found!");
        }
    }
}

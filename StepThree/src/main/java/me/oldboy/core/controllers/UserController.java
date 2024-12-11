package me.oldboy.core.controllers;

import lombok.RequiredArgsConstructor;
import me.oldboy.annotations.Auditable;
import me.oldboy.annotations.Loggable;
import me.oldboy.core.dto.users.UserCreateDto;
import me.oldboy.core.dto.users.UserReadDto;
import me.oldboy.core.dto.users.UserUpdateDeleteDto;
import me.oldboy.core.model.database.audit.operations.AuditOperationType;
import me.oldboy.exception.UserControllerException;
import me.oldboy.core.model.service.SecurityService;
import me.oldboy.core.model.service.UserService;
import me.oldboy.security.JwtAuthResponse;
import me.oldboy.validate.ValidatorDto;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityService securityService;

    @Loggable
    public boolean registrationUser(UserCreateDto userCreateDto){
        /* Проверяем входящие данные */
        ValidatorDto.getInstance().isValidData(userCreateDto);

        /* Проверяем не дублируются ли данные */
        if(userService.findByUserName(userCreateDto.userName()).isPresent()){
            throw new UserControllerException("User with name ' " + userCreateDto.userName() + " ' is already exist! " +
                                              "Пользователь с именем ' " + userCreateDto.userName() + " ' уже существует!");
        } else {
            userService.create(userCreateDto);
        }
            return true;
    }

    @Loggable
    public JwtAuthResponse loginUser(String login, String password){
        if(userService.findByUserName(login).isEmpty()){
            throw new UserControllerException("Login '" + login + "' not found! " +
                                              "Пользователь с логином '" + login + "' не найден!");
        }
        return securityService.loginUser(login, password);
    }

    @Loggable
    @Auditable(operationType = AuditOperationType.DELETE_USER)
    public boolean deleteUser(UserUpdateDeleteDto deleteDto, String userName){
        ValidatorDto.getInstance().isValidData(deleteDto);

        Optional<UserReadDto> userForRemove =
                userService.findByUserNameAndPassword(deleteDto.userName(), deleteDto.password());

        if(userForRemove.isEmpty()){
            throw new UserControllerException("Have no user to remove! Пользователь для удаления не найден!");
        }
        if(userForRemove.get().userId() != deleteDto.userId()){
            throw new UserControllerException("Data not congruent! Уверенны что хотите удалить именно этого пользователя?");
        }

        return userService.delete(userForRemove.get().userId());
    }

    @Loggable
    @Auditable(operationType = AuditOperationType.UPDATE_USER)
    public boolean updateUser(UserUpdateDeleteDto updateDto, String userName){
        /* Все проверки на уровне сервисов */
        return userService.update(updateDto.userId(), updateDto);
    }

    @Loggable
    public List<UserReadDto> getAllUser(){
        return userService.findAll();
    }
}
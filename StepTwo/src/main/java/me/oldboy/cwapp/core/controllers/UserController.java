package me.oldboy.cwapp.core.controllers;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.exceptions.controllers.UserControllerException;
import me.oldboy.cwapp.core.entity.User;
import me.oldboy.cwapp.core.service.UserService;

import java.util.Scanner;

@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    public boolean registrationUser(Scanner scanner){
        System.out.println("Регистрация нового пользователя.");
        String[] registrationData = enterLoginAndPassMenu(scanner);

        User regUser = new User(registrationData[0], registrationData[1]);
        Long regId = userService.createUser(regUser);
        if(regId == null){
            throw new UserControllerException("Вероятно логин: " + registrationData[0] + " уже занят!");
        } else {
            System.out.println("Пользователь успешно зарегистрирован!" +
                               "\n---------------------------------------------------------------------\n");
        }
        return true;
    }

    public Long loginUser(Scanner scanner){
        System.out.println("Вход в систему.");
        String[] loginData = enterLoginAndPassMenu(scanner);

        User mayBeLogIn = userService.findUserByLoginAndPassword(loginData[0], loginData[1]);
        if(mayBeLogIn == null){
            throw new UserControllerException("Вероятно логин/пароль введены неверно!");
        } else {
            System.out.println("Вы вошли в систему!" +
                               "\n---------------------------------------------------------------------\n");
        }
        return mayBeLogIn.getUserId();
    }

    private String[] enterLoginAndPassMenu(Scanner scanner){
        String[] loginAndPass = new String[2];
        System.out.print("Введите логин: ");
        String enterLogin = scanner.nextLine().trim();
        loginAndPass[0] = enterLogin;
        System.out.print("Введите пароль: ");
        String enterPassword = scanner.nextLine().trim();
        loginAndPass[1] = enterPassword;
        return loginAndPass;
    }
}

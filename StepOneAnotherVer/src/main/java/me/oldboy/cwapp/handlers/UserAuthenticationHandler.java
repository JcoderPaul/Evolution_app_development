package me.oldboy.cwapp.handlers;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.entity.Role;
import me.oldboy.cwapp.entity.User;
import me.oldboy.cwapp.exception.handlers_exception.UserAuthenticationHandlerException;
import me.oldboy.cwapp.services.UserService;

import java.util.Scanner;

@RequiredArgsConstructor
public class UserAuthenticationHandler {

    private final UserService userService;

    public boolean registrationUser(Scanner scanner){
        System.out.println("Регистрация нового пользователя.");
        String[] registrationData = enterLoginAndPassMenu(scanner);

        User regUser = new User(registrationData[0],
                                registrationData[1],
                                Role.USER);
        Long regId = userService.registration(regUser);
        if(regId == null){
            throw new UserAuthenticationHandlerException("Вероятно логин: " +
                                                         registrationData[0] +
                                                         " уже занят!");
        } else {
            System.out.println("Пользователь успешно зарегистрирован!" +
                               "\n---------------------------------------------------------------------\n");
        }
            return true;
    }

    public Long loginUser(Scanner scanner){
        System.out.println("Вход в систему.");
        String[] loginData = enterLoginAndPassMenu(scanner);

        User mayBeLogIn = userService.login(loginData[0],
                                            loginData[1]);
        if(mayBeLogIn == null){
            throw new UserAuthenticationHandlerException("Вероятно логин/пароль введены неверно!");
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

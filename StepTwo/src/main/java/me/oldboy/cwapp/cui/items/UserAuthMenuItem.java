package me.oldboy.cwapp.cui.items;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.core.controllers.UserController;

import java.util.Scanner;

@RequiredArgsConstructor
public class UserAuthMenuItem {

    private final UserController userController;
    private Long userId;

    public Long regAndLogin(Scanner scanner){
        Boolean isEntering = true;
        System.out.println("Пожалуйста зарегистрируйтесь если вы впервые у нас!");
        do{
            System.out.print("Выберите один из пунктов меню: " +
                               "\n1 - регистрация;" +
                               "\n2 - вход в систему;" +
                               "\n3 - покинуть программу;\n\n" +
                               "Сделайте выбор и нажмите ввод: ");
            String choiceMenuItem = scanner.nextLine();

            switch (choiceMenuItem) {
                case "1":
                    userController.registrationUser(scanner);
                    break;
                case "2":
                    userId = userController.loginUser(scanner);
                    isEntering = false;
                    break;
                case "3":
                    userId = null;
                    isEntering = false;
                    break;
            }
        } while (isEntering);
        return userId;
    }
}

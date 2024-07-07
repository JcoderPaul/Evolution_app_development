package me.oldboy.input.controllers;

import me.oldboy.input.entity.User;
import me.oldboy.input.exeptions.UserControllerException;
import me.oldboy.input.repository.UserBase;

/**
 * Controller for user manage.
 */
public class UserController {

    private UserBase userBase;

    public UserController(UserBase userBase) {
        this.userBase = userBase;
    }

    /**
     * Create new User.
     *
     * @param login         the login for registration
     * @throws UserControllerException if enter null or empty login
     */
    public boolean createUser(String login){
        if (login == null || login == "") {
            throw new UserControllerException("Вы ничего не ввели");
        }
        boolean isCreate = true;
        if(userBase.createUser(login).isEmpty()){
            isCreate = false;
        }
        return isCreate;
    }

    /**
     * Login in coworking system
     *
     * @param login         the login for entering
     * @throws UserControllerException if enter null or empty login
     * @throws UserControllerException if not have login in coworking system
     */
    public User login(String login){
        if (login == null || login == "") {
            throw new UserControllerException("Логин не может быть пустым");
        }
        if(userBase.login(login).isEmpty()){
            throw new UserControllerException("Пользователя с таким логином НЕ существует!");
        } else
            return userBase.login(login).get();
    }
}

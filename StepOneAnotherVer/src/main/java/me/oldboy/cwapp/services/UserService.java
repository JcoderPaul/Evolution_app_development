package me.oldboy.cwapp.services;

import me.oldboy.cwapp.store.base.UserBase;
import me.oldboy.cwapp.entity.User;
import me.oldboy.cwapp.exception.service_exception.UserServiceException;

import java.util.Optional;

public class UserService {

    private UserBase userBase;

    private UserService(UserBase userBase) {
        this.userBase = userBase;
    }

    public Long registration(User user){
        Long regId = null;
        if ((userBase.findUserByLogin(user.getUserLogin()).isEmpty()) && (user.getUserId() == null)){
            regId = userBase.create(user);
        } else {
            throw new UserServiceException("Логин: " + user.getUserLogin() + "уже есть в системе или " +
                                           "переданные данные содержат недопустимые значения, например ID!");
        }
        return regId;
    }

    public User login(String login, String passWord){
        Optional<User> loggedInUser = userBase.findUserByLogin(login);
        if(loggedInUser.isEmpty()) {
            throw new UserServiceException("Пользователь не зарегистрирован!");
        } else if (!loggedInUser.get().getPassWord().equals(passWord)) {
            throw new UserServiceException("Неверный пароль!");
        } else
            return loggedInUser.get();
    }
}

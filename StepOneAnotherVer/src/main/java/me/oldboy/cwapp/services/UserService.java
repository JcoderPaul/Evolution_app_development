package me.oldboy.cwapp.services;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.store.base.UserBase;
import me.oldboy.cwapp.entity.User;
import me.oldboy.cwapp.exception.service_exception.UserServiceException;
import me.oldboy.cwapp.store.repository.UserRepository;

import java.util.Optional;

@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Long registration(User user){
        Long regId = null;
        if ((userRepository.findUserByLogin(user.getUserLogin()).isEmpty()) && (user.getUserId() == null)){
            regId = userRepository.create(user);
        } else {
            throw new UserServiceException("Логин: " + user.getUserLogin() + " уже есть в системе или " +
                                           "переданные данные содержат недопустимые значения, например ID!");
        }
        return regId;
    }

    public User login(String login, String passWord){
        Optional<User> loggedInUser = userRepository.findUserByLogin(login);
        if(loggedInUser.isEmpty()) {
            throw new UserServiceException("Пользователь не зарегистрирован!");
        } else if (!loggedInUser.get().getPassWord().equals(passWord)) {
            throw new UserServiceException("Неверный пароль!");
        } else
            return loggedInUser.get();
    }

    public User getExistUserById(Long userId){
        Optional<User> mayBeUser = userRepository.findById(userId);
        if(mayBeUser.isEmpty()){
            throw new UserServiceException("Пользователь c ID: " + userId + " не найден!");
        } else
            return mayBeUser.get();
    }
}

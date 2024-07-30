package me.oldboy.cwapp.input.service;

import lombok.RequiredArgsConstructor;
import me.oldboy.cwapp.input.entity.User;
import me.oldboy.cwapp.exceptions.services.UserServiceException;
import me.oldboy.cwapp.input.repository.crud.ReservationRepository;
import me.oldboy.cwapp.input.repository.crud.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * User service layer - getting pure entities.
 */
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Create (save) new User to base.
     *
     * @param user the new user for creating
     *
     * @return new created (and save to base) User
     */
    public Long createUser(User user) {
        if(userRepository.findUserByLogin(user.getLogin()).isPresent()){
            throw new UserServiceException("Пользователь не создан, логин '" +
                                           user.getLogin() +
                                           "' существует в системе!");
        } else
            return userRepository.createUser(user).get().getUserId();
    }

    /**
     * Получаем всех пользователей
     *
     * Find all users in base.
     *
     * @return list of all users finding in base
     */
    public List<User> findAllUser() {
        List<User> allUsers = userRepository.findAllUsers();
        if(allUsers.size() == 0){
            throw new UserServiceException("База пользователей пуста!");
        } else
            return allUsers;
    }

    /**
     * Получаем пользователя по ID
     *
     * Find user by ID.
     *
     * @return user finding in base
     */
    public User findUserById(Long id) {
        Optional<User> mayBeUser = userRepository.findUserById(id);
        if(mayBeUser.isEmpty()){
            throw new UserServiceException("Пользователь с ID - " + id + " не найден!");
        } else
            return mayBeUser.get();
    }

    /**
     * Получаем пользователя по Login-у
     *
     * Find user by Login.
     *
     * @return user finding in base by login
     */
    public User findUserByLogin(String login) {
        Optional<User> mayByUser = userRepository.findUserByLogin(login);
        if(mayByUser.isEmpty()){
            throw new UserServiceException("Пользователь с login-ом: '" + login + "' не найден!");
        } else
            return mayByUser.get();
    }

    /**
     * Получаем пользователя по полному совпадению сочетания логин-пароль
     *
     * Find user by login and password.
     *
     * @param login   the user login
     * @param password   the user password
     *
     * @return user finding in base
     */
    public User findUserByLoginAndPassword(String login, String password) {
        Optional<User> mayBeUser = userRepository.findUserByLoginAndPassword(login, password);
        if(mayBeUser.isEmpty()){
            throw new UserServiceException("Пользователь с таким паролем и логином не " +
                                           "найден или данные введены не верно");
        } else
            return mayBeUser.get();
    }

    /**
     * Update existing user.
     *
     * @param user the user for update
     *
     * @return true - if the user was successfully update, and
     *         false - if an error occurred
     */
    public boolean updateUser(User user) {
        boolean mayBeUpdate = false;
        if(userRepository.findUserById(user.getUserId()).isPresent()){

            mayBeUpdate = userRepository.updateUser(user);
        } else {
            throw new UserServiceException("Пользователь с ID - " +
                                           user.getUserId() + " в системе не найден, " +
                                           "обновление данных невозможно");
        }
        return mayBeUpdate;
    }

    /**
     * Delete existing user from base.
     *
     * @param user the user to delete from base
     *
     * @return true - if the user was successfully deleted, and
     *         false - if an error occurred
     */
    public boolean deleteUser(User user) {
        boolean mayBeDelete = false;
        if(userRepository.findUserById(user.getUserId()).isPresent() &&
                      reservationRepository.findReservationByUserId(user.getUserId()).get().size() == 0){

            mayBeDelete = userRepository.deleteUser(user.getUserId());
        } else {
            throw new UserServiceException("Пользователь с ID - " +
                                           user.getUserId() + " в системе не найден." +
                                           "Или у него есть зарезервированные " +
                                           "рабочие места или залы. Снимите бронь!");
        }
        return mayBeDelete;
    }
}

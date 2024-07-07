package me.oldboy.input.repository;

import me.oldboy.input.entity.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * User Base for save existing User.
 */
public class UserBase {

    public UserBase() {
    }

    private final Map<String, User> usersBase = new HashMap<>();
    private final User admin = new User("Admin", "admin");

    public void initBaseAdmin(){
        usersBase.put(admin.getLogin(), admin);
    }

    /**
     * Create new User.
     *
     * @param newLogin    the new User login for future authorization
     * @return optional User
     */
    public Optional<User> createUser(String newLogin){
        User regUser = null;
        if(!usersBase.containsKey(newLogin)){
            regUser = new User(newLogin);
            usersBase.put(newLogin, regUser);
        }
        return Optional.ofNullable(regUser);
    }

    /**
     * Login User.
     *
     * @param enterLogin   the login for authorization
     * @return optional User
     */
    public Optional<User> login(String enterLogin){
        User mayBeUser = null;
        if(usersBase.containsKey(enterLogin)){
            mayBeUser = usersBase.get(enterLogin);
        }
        return Optional.ofNullable(mayBeUser);
    }

    /**
     * Get user base.
     *
     * @return map of all Users
     */
    public Map<String, User> getUsersBase() {
        return usersBase;
    }

    public boolean isUserRegister(String login){
        return usersBase.containsKey(login);
    }
}
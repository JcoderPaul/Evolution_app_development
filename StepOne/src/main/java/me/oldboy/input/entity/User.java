package me.oldboy.input.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * User entity
 */
public class User {
    private String login;
    private String status;

    private final Map<Integer, ReserveUnit> userReservedUnitList = new HashMap<>();

    public User(String login) {
        this.login = login;
        this.status = "user";
    }

    public User(String login, String status) {
        this.login = login;
        this.status = status;
    }

    /**
     * Get user login
     *
     * @return user login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Get user status
     *
     * @return user status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get user all user slots reservation
     *
     * @return a map of all user slots reservation
     */
    public Map<Integer, ReserveUnit> getUserReservedUnitList() {
        return userReservedUnitList;
    }
}

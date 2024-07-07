package me.oldboy.junit.input.controllers;

import me.oldboy.input.controllers.UserController;
import me.oldboy.input.entity.User;
import me.oldboy.input.repository.UserBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UserController.
 */
class UserControllerTest {
    private static UserBase userBase = new UserBase();;
    private UserController userController = new UserController(userBase);
    private static String godUserLogin;
    private static String emptyUserLogin;
    private static String alreadyExistUserLogin;

    @BeforeAll
    public static void setUp() {
        userBase.initBaseAdmin();
        godUserLogin = "User";
        emptyUserLogin = "";
        alreadyExistUserLogin = "Admin";
    }

    @AfterAll
    public static void finalCleanReserveAndUserDataBase() {
        for (Map.Entry<String, User> erasure : userBase.getUsersBase().entrySet()) {
            erasure.getValue().getUserReservedUnitList().clear();
        }
    }

    @Test
    @DisplayName("Should return true when user is create")
    public void goodCreateUserTest(){
        assertTrue(userController.createUser(godUserLogin));
    }

    @Test
    @DisplayName("Should return exception when user enter empty login to create-user form")
    public void emptyCreateLoginTest(){
        assertThrows(IllegalArgumentException.class, () -> userController.createUser(emptyUserLogin));
    }

    @Test
    @DisplayName("Should return exception when user enter null login to create-user form")
    public void nullCreateLoginTest(){
        assertThrows(IllegalArgumentException.class, () -> userController.createUser(null));
    }

    @Test
    @DisplayName("Should return false when try to create existing login")
    public void alreadyExistCreateLoginTest(){
        userController.createUser(godUserLogin);
        assertFalse(userController.createUser(godUserLogin));
        assertFalse(userController.createUser(alreadyExistUserLogin));
    }

    @Test
    @DisplayName("Should return same login if user enter an existing login in user base")
    public void alreadyExistingUserLoginTest(){
        User resUser = userController.login(alreadyExistUserLogin);
        assertEquals(resUser, userBase.getUsersBase().get(resUser.getLogin()));
    }

    @Test
    @DisplayName("Should return exception when try to enter not existing login")
    public void notExistingLoginUserTest(){
        assertThrows(RuntimeException.class, () -> userController.login(godUserLogin));
    }

    @Test
    @DisplayName("Should return exception when user enter empty login to login form")
    public void emptyEnterLoginTest(){
        assertThrows(IllegalArgumentException.class, () -> userController.login(emptyUserLogin));
    }

    @Test
    @DisplayName("Should return exception when user enter null login to login form")
    public void nullEnterLoginTest(){
        assertThrows(IllegalArgumentException.class, () -> userController.login(null));
    }
}
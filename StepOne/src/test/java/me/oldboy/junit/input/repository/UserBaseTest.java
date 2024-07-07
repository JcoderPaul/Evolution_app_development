package me.oldboy.junit.input.repository;

import me.oldboy.input.entity.User;
import me.oldboy.input.repository.UserBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UserBase.
 */
class UserBaseTest {

    private static UserBase userBase;
    private static String simpleUser;
    private static String alreadyExistUser;

    @BeforeAll
    public static void setUp() {
        userBase = new UserBase();
        userBase.initBaseAdmin();
        simpleUser = "User";
        alreadyExistUser = userBase.getUsersBase().get("Admin").getLogin();
    }

    @AfterEach
    public void cleanReserveAndHallDataBase() {
        userBase.getUsersBase().clear();
        userBase.initBaseAdmin();
    }

    @Test
    public void createNotExistNewUser(){
        Optional<User> mayBeNewUser= userBase.createUser(simpleUser);
        assertEquals(mayBeNewUser.get(), userBase.getUsersBase().get(simpleUser));
    }

    @Test
    public void tryToCreateAnotherExistingUser(){
        assertTrue(userBase.createUser(alreadyExistUser).isEmpty());

        userBase.createUser(simpleUser);
        assertTrue(userBase.createUser(simpleUser).isEmpty());
    }

    @Test
    public void tryLoginExistUser(){
        assertTrue(userBase.login(alreadyExistUser).isPresent());
    }

    @Test
    public void tryLoginNonExistUser(){
        assertFalse(userBase.login(simpleUser).isPresent());
    }

}
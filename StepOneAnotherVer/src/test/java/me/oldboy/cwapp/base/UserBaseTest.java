package me.oldboy.cwapp.base;

import me.oldboy.cwapp.entity.User;
import me.oldboy.cwapp.exception.base_exception.UserBaseException;
import me.oldboy.cwapp.store.base.UserBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static me.oldboy.cwapp.entity.Role.ADMIN;
import static me.oldboy.cwapp.entity.Role.USER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class UserBaseTest {

    public UserBase userBase;

    @BeforeEach
    public void initBase(){
        userBase = new UserBase();
    }

    @AfterEach
    public void killBase(){
        userBase.getUserBase().clear();
    }

    /* Проверяем нормальную работу методов */

    @Test
    void createUserGoodTest() {
        Long newUserId = userBase.create(new User("Admin", "admin", ADMIN));
        assertThat(newUserId).isEqualTo(1L);
        Long newUserId_2 = userBase.create(new User("User", "user", USER));
        assertThat(newUserId_2).isEqualTo(2L);
        Long newUserId_3 = userBase.create(new User("User", "user", USER));
        assertThat(newUserId_3).isEqualTo(3L);
    }

    @Test
    void findByIdUserGoodTest() {
        User newUserCreate = new User("Admin", "admin", ADMIN);
        Long newUserId = userBase.create(newUserCreate);

        Boolean mayBeUser = userBase.findById(newUserId).isPresent();
        assertThat(mayBeUser).isTrue();
        assertThat(userBase.findById(newUserId).get()).isEqualTo(newUserCreate);
    }

    @Test
    void deleteUserGoodTest() {
        User userForCreate = new User("BigAdmin", "admin", ADMIN);
        Long createdUserId = userBase.create(userForCreate);

        assertThat(userBase.delete(createdUserId)).isTrue();
    }

    @Test
    void updateUserGoodTest() {
        User newUserCreate = new User("Admin", "admin", ADMIN);
        Long newUserId = userBase.create(newUserCreate);

        User userForUpdate = userBase.findById(newUserId).get();
        userForUpdate.setUserLogin("NotAdmin");
        userForUpdate.setPassWord("1234");
        userForUpdate.setRole(USER);

        assertThat(userBase.update(userForUpdate)).isEqualTo(userForUpdate);
    }

    @Test
    void findUserByLoginGoodTest() {
        Long newUserId = userBase.create(new User("Adminushka", "admin", ADMIN));
        Long newUserId_2 = userBase.create(new User("UserOne", "user", USER));
        Long newUserId_3 = userBase.create(new User("UserTwo", "user", USER));

        assertThat(userBase.findUserByLogin("UserOne")).isEqualTo(userBase.findById(newUserId_2));
        assertThat(userBase.findUserByLogin("Adminushka")).isEqualTo(userBase.findById(newUserId));
        assertThat(userBase.findUserByLogin("UserTwo")).isEqualTo(userBase.findById(newUserId_3));
    }

    @Test
    void findAllGoodTest() {
        Long newUserId = userBase.create(new User("Joker", "admin", ADMIN));
        Long newUserId_2 = userBase.create(new User("BatMan", "1234", USER));
        Long newUserId_3 = userBase.create(new User("Dent", "4321", USER));
        Long newUserId_4 = userBase.create(new User("CatWomen", "5678", USER));
        Long newUserId_5 = userBase.create(new User("Penguin", "8765", USER));

        assertThat(userBase.findAll().size()).isEqualTo(5);

        assertThat(userBase.findAll().contains(userBase.findById(newUserId).get())).isTrue();
        assertThat(userBase.findAll().contains(userBase.findById(newUserId_2).get())).isTrue();
        assertThat(userBase.findAll().contains(userBase.findById(newUserId_3).get())).isTrue();
        assertThat(userBase.findAll().contains(userBase.findById(newUserId_4).get())).isTrue();
        assertThat(userBase.findAll().contains(userBase.findById(newUserId_5).get())).isTrue();
    }

    /* Проверяем броски исключений */

    @Test
    void createUserWithIdExceptionTest() {
        assertThatThrownBy(()->userBase.create(new User(1L,"Admin", "admin", ADMIN)))
                .isInstanceOf(UserBaseException.class)
                .hasMessageContaining("Ошибка создания нового пользователя!");
    }

    @Test
    void findNonexistentUserByIdExceptionTest() {
       Long nonExistentUserId = 2L;
       assertThat(userBase.findById(nonExistentUserId)).isEqualTo(Optional.ofNullable(null));
    }

    @Test
    void updateNonExistentUserExceptionTest() {
        User updateUser = new User(3L, "Chebur", "1234", USER);

        assertThatThrownBy(()->userBase.update(updateUser))
                .isInstanceOf(UserBaseException.class)
                .hasMessageContaining("Вы пытаетесь обновить несуществующего пользователя!");
    }

    @Test
    void deleteNonExistentUserExceptionTest() {
        User deleteUser = new User(5L, "Gena", "4321", USER);
        assertThatThrownBy(()->userBase.delete(deleteUser.getUserId()))
                .isInstanceOf(UserBaseException.class)
                .hasMessageContaining("Пользователь с ID: " + deleteUser.getUserId() + " в базе не найден!");
    }

    @Test
    void findNonExistentUserByLoginTest() {
        User findUser = new User(8L, "Lariska", "4321", USER);
        assertThat(userBase.findUserByLogin(findUser.getUserLogin()))
                .isEqualTo(Optional.empty());
    }
}
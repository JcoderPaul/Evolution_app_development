package me.oldboy.cwapp.base;

import lombok.Getter;
import me.oldboy.cwapp.entity.User;
import me.oldboy.cwapp.exception.UserBaseException;
import me.oldboy.cwapp.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class UserBase implements UserRepository {

    private Map<Long, User> userBase = new HashMap<>();

    /**
     * Создаем нового User-a.
     *
     * Генерация ID идет на старте исходя из размера БД,
     * затем исходя из максимального значения ID имеющегося в БД.
     * Проверки дубликатов Login-a не проводится, она на слой или
     * два выше например в service или controller.
     *
     * @param user пользователь для создания
     * @throws UserBaseException если произошла ошибка генерации ID
     * @throws UserBaseException если произошла ошибка создания нового пользователя
     * @return значение ID созданного пользователя
     */
    @Override
    public Long create(User user) {
        Long generateId = null;
        if(user.getUserId() == null && userBase.size() == 0){
            generateId = 1L;
            user.setUserId(generateId);
            userBase.put(generateId, user);
        } else if (user.getUserId() == null && userBase.size() > 0){
            generateId = 1L + userBase.keySet()
                                      .stream()
                                      .mapToLong(key->key)
                                      .max()
                                      .orElseThrow(() -> new UserBaseException("Сбой в генерации ID!"));
            user.setUserId(generateId);
            userBase.put(generateId, user);
        } else {
            throw new UserBaseException("Ошибка создания нового пользователя!");
        }
        return generateId;
    }

    /**
     * Ищет существующего пользователя по ID.
     *
     * @param userId значение ID пользователя для поиска
     * @return возвращает пользователя в случае нахождения
     *         и null в противном случае
     */
    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(userBase.get(userId));
    }

    /**
     * Обновляет существующего пользователя.
     * В метод передается User с уже существующим ID,
     * но новыми (Login/Password/Role)
     *
     * @param user пользователь для обновления с известным ID,
     *             но другими данными нежели в БД
     * @return обновленные данные пользователя
     */
    @Override
    public User update(User user) {
        if(userBase.containsKey(user.getUserId())) {
            delete(user.getUserId());
            userBase.put(user.getUserId(), user);
        } else {
            throw new UserBaseException("Вы пытаетесь обновить несуществующего пользователя!");
        }
        return userBase.get(user.getUserId());
    }

    /**
     * Удаляет существующего пользователя.
     *
     * @param userId ID пользователя для удаления из БД
     * @throws UserBaseException если произошла ошибка удаления пользователя
     * @return true - если удаление прошло успешно, false - если удаление не прошло
     */
    @Override
    public boolean delete(Long userId) {
        if(userBase.containsKey(userId)){
           userBase.remove(userId);
        } else {
            throw new UserBaseException("Пользователь с ID: " + userId + " в базе не найден!");
        }
        return true;
    }

    @Override
    public Optional<User> findUserByLogin(String userLogin) {
        return Optional.ofNullable(userBase.entrySet().stream()
                .map(k -> k.getValue())
                .filter(u -> u.getUserLogin().equals(userLogin))
                .findAny()
                .orElseThrow(() -> new UserBaseException("Пользователь с таким логином не найден!")));
    }

    @Override
    public List<User> findAll() {
        return userBase.entrySet().stream()
                .map(k -> k.getValue())
                .collect(Collectors.toList());
    }
}

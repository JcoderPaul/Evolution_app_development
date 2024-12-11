package me.oldboy.core.model.service;

import me.oldboy.core.dto.users.UserCreateDto;
import me.oldboy.core.dto.users.UserReadDto;
import me.oldboy.core.dto.users.UserUpdateDeleteDto;
import me.oldboy.core.model.database.entity.options.Role;
import me.oldboy.exception.UserServiceException;
import me.oldboy.core.mapper.UserMapper;
import me.oldboy.core.model.database.entity.User;
import me.oldboy.core.model.database.repository.UserRepository;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;
import me.oldboy.validate.ValidatorDto;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserService extends ServiceBase<Long, User>{

    public UserService(RepositoryBase<Long, User> repositoryBase) {
        super(repositoryBase);
    }

    @Transactional
    public Long create(UserCreateDto userCreateDto) {
        User userTransferToRepository = UserMapper.INSTANCE.mapToEntity(userCreateDto);
        return getRepositoryBase().create(userTransferToRepository).getUserId();
    }

    @Transactional
    public boolean delete(Long id) {
        Optional<User> maybeUser = getRepositoryBase().findById(id);
        maybeUser.ifPresent(user -> getRepositoryBase().delete(user.getUserId()));
        return maybeUser.isPresent();
    }

    @Transactional
    public boolean update(Long userId, UserUpdateDeleteDto updateDto) {
        /* Проверяем наличие записи в БД для изменения */
        Optional<User> maybeUser = getRepositoryBase().findById(userId);

        /* Проверяем входящие данные */
        ValidatorDto.getInstance().isValidData(updateDto);

        /* Если данные валидны и запись есть меняем содержимое */
        maybeUser.ifPresent(user -> { user.setUserName(updateDto.userName());
                                      user.setPassword(updateDto.password());
                                      user.setRole(updateDto.role().transform(s -> Role.valueOf(s.toUpperCase())));
                                      getRepositoryBase().update(user);});

        /* Подтверждаем обновление */
        return maybeUser.isPresent();
    }

    @Transactional
    public Optional<UserReadDto> findById(Long id) {
        return getRepositoryBase()
                .findById(id)
                .map(UserMapper.INSTANCE::mapToUserReadDto);
    }

    @Transactional
    public List<UserReadDto> findAll() {
        return getRepositoryBase()
                .findAll()
                .stream()
                .map(UserMapper.INSTANCE::mapToUserReadDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<UserReadDto> findByUserName(String userName) {
        return ((UserRepository) getRepositoryBase())
                .findUserByLogin(userName)
                .map(UserMapper.INSTANCE::mapToUserReadDto);
    }

    @Transactional
    public Optional<UserReadDto> findByUserNameAndPassword(String userName, String password) {
        Optional<User> mayBeUser =
                ((UserRepository) getRepositoryBase()).findUserByLoginAndPassword(userName, password);
        if (mayBeUser.isEmpty()){
            throw new UserServiceException("Password or login is incorrect! Вы ввели неверный пароль или логин!");
        }
        return mayBeUser.map(UserMapper.INSTANCE::mapToUserReadDto);
    }
}
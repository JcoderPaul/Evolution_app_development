package me.oldboy.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.dto.users.UserCreateDto;
import me.oldboy.dto.users.UserReadDto;
import me.oldboy.dto.users.UserUpdateDeleteDto;
import me.oldboy.exception.user_exception.UserServiceException;
import me.oldboy.logger.annotation.Measurable;
import me.oldboy.mapper.UserMapper;
import me.oldboy.models.entity.User;
import me.oldboy.models.entity.options.Role;
import me.oldboy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@NoArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Measurable
    public Long create(UserCreateDto userCreateDto) {
        User createdUser = UserMapper.INSTANCE.mapToEntity(userCreateDto);

        /* Незабываем зашифровать переданный в UserCreateDto открытый пароль */
        String encodedPass = passwordEncoder.encode(userCreateDto.password());
        createdUser.setPassword(encodedPass);

        /* Уже с зашифрованным паролем сохраняем данные в БД */
        User userFromBase = userRepository.save(createdUser);

        return userFromBase.getUserId();
    }

    @Transactional
    @Measurable
    public boolean delete(Long id) {
        Optional<User> maybeUser = userRepository.findById(id);
        if (maybeUser.isPresent()) {
            userRepository.delete(maybeUser.get());
        } else {
            throw new UserServiceException("User with id - " + id + " not found!");
        }
        return maybeUser.isPresent();
    }

    @Transactional
    @Measurable
    public boolean update(UserUpdateDeleteDto updateDto) {
        /* Проверяем наличие записи в БД для изменения */
        Optional<User> maybeUser = userRepository.findById(updateDto.userId());

        /* Если данные валидны и запись есть, меняем содержимое */
        maybeUser.ifPresentOrElse(user -> {
                    user.setLogin(updateDto.login());
                    user.setPassword(updateDto.password());
                    user.setRole(updateDto.role().transform(s -> Role.valueOf(s.toUpperCase())));
                    userRepository.save(user);
                }, () -> {
                    throw new UserServiceException("User id - " + updateDto.userId() + " not found!");
                }
        );

        /* Подтверждаем обновление */
        return maybeUser.isPresent();
    }

    @Measurable
    public Optional<UserReadDto> findById(Long id) {
        return userRepository.findById(id).map(UserMapper.INSTANCE::mapToUserReadDto);
    }

    @Measurable
    public List<UserReadDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper.INSTANCE::mapToUserReadDto)
                .collect(Collectors.toList());
    }

    @Measurable
    public Optional<UserReadDto> findByLogin(String login) {
        return userRepository.findByLogin(login).map(UserMapper.INSTANCE::mapToUserReadDto);
    }
}
package me.oldboy.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.dto.UserCreateDto;
import me.oldboy.dto.UserReadDto;
import me.oldboy.entity.User;
import me.oldboy.entity.options.Role;
import me.oldboy.mapper.UserMapper;
import me.oldboy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Service
/*
Если предположить, что наш API работает только на чтение (возвращение) данных по запросу, то установка параметра
readOnly = true, для всех методов класса идеальное решение, но у нас есть пара методов которые предназначены для
добавления и удаления записей в/из БД. Поэтому эти методы будут проаннотированы повторно и уже без параметра, что
позволит вносить требуемые изменения.
*/
@Transactional(readOnly = true)
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<UserReadDto> getUser(Long id) {
        return userRepository.findById(id)
                             .map(user -> UserMapper.INSTANCE.mapToUserReadDto(user));
    }

    @Transactional
    public UserReadDto createUser(UserCreateDto userCreateDto) {
       User forCreateUser = UserMapper.INSTANCE.mapToEntity(userCreateDto);
       return UserMapper.INSTANCE.mapToUserReadDto(userRepository.save(forCreateUser));
    }

    public List<UserReadDto> getAllUsers() {
        return userRepository.findAll().stream()
                                       .map(user -> UserMapper.INSTANCE.mapToUserReadDto(user))
                                       .collect(Collectors.toList());
    }

    @Transactional
    public boolean deleteUser(Long id) {
        Optional<User> userForDelete = userRepository.findById(id);
        userForDelete.ifPresent(user -> userRepository.delete(user));
        return userForDelete.isPresent();
    }

    public boolean isUserExist(String userName){
        return userRepository.findByUserName(userName).isPresent();
    }

    /*
    В данной ситуации (небольшое приложение, одна сущность) мы могли бы написать пару методов Mapper-ов,
    для экспресс преобразования DTO в Entity и обратно (или вынести их в отдельный класс) и получить,
    что-то вроде:

    private UserReadDto userReadMapper(User user){
        return UserReadDto.builder()
                          .userId(user.getUserId())
                          .userName(user.getUserName())
                          .role(user.getRole())
                          .build();
    }

    private User userCreateMapper(UserCreateDto createDto){
        return User.builder()
                .userName(createDto.getUserName())
                .password(createDto.getPassword())
                .role(Role.valueOf(createDto.getRole()))
                .build();
    }

    И все же применим MapStruct - генератор кода, который упрощает реализацию сопоставлений между типами
    компонентов Java (Spring) приложения.
    */
}

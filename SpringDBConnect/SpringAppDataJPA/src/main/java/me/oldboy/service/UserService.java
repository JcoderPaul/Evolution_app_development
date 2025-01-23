package me.oldboy.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.dto.UserReadDto;
import me.oldboy.entity.User;
import me.oldboy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Service
/*
Если предположить, что наш API работает только на чтение (возвращение) данных по запросу, то установка параметра
readOnly = true, для всех методов класса идеальное решение, но у нас есть пара методов которые предназначены для
добавления и удаления записей в/из БД. Поэтому эти методы будут проаннотированны повторно и уже без параметра, что
позволит вносить требуемые изменения.
*/
@Transactional(readOnly = true)
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserReadDto getUser(Long id) {
        return userRepository.findById(id)
                             .map(this::miniMapper)
                             .orElse(UserReadDto.builder()
                                                .userId(0L)
                                                .userName("Have no User (Unexpected ID)!")
                                                .build());
    }

    public boolean isUserExist(String userName){
        return userRepository.findByUserName(userName).isPresent();
    }

    @Transactional
    public UserReadDto createUser(User user) {
       return miniMapper(userRepository.save(user));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public boolean deleteUser(Long id) {
        Optional<User> userForDelete = userRepository.findById(id);
        userForDelete.ifPresent(user -> userRepository.delete(user));
        return userForDelete.isPresent();
    }

    /*
    Не будем выделять отдельный класс Mapper, сделаем экспресс преобразование - вернем данные
    пользователя без пароля, в прошлой реализации мы возвращали null, теперь красивые данные
    без пустых полей.
    */
    private UserReadDto miniMapper(User user){
        return UserReadDto.builder()
                          .userId(user.getUserId())
                          .userName(user.getUserName())
                          .role(user.getRole())
                          .build();
    }
}

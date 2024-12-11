package me.oldboy.core.model.service;

import me.oldboy.exception.SecurityServiceException;
import me.oldboy.core.model.database.entity.User;
import me.oldboy.core.model.database.repository.UserRepository;
import me.oldboy.core.model.database.repository.crud.RepositoryBase;
import me.oldboy.security.JwtAuthResponse;
import me.oldboy.security.JwtTokenGenerator;

import javax.transaction.Transactional;
import java.util.Optional;

public class SecurityService extends ServiceBase<Long, User>{

    private final JwtTokenGenerator jwtTokenGenerator;

    public SecurityService(RepositoryBase<Long, User> repositoryBase, JwtTokenGenerator jwtTokenGenerator) {
        super(repositoryBase);
        this.jwtTokenGenerator = jwtTokenGenerator;
    }

    @Transactional
    public JwtAuthResponse loginUser(String login, String password) {
        Optional<User> mayBeUserInBase =
                ((UserRepository) getRepositoryBase()).findUserByLoginAndPassword(login, password);

        if (mayBeUserInBase.isEmpty()) {
            throw new SecurityServiceException("Wrong password! Неверный пароль!");
        }

        String token = jwtTokenGenerator.getToken(mayBeUserInBase.get().getUserId(),
                                                  mayBeUserInBase.get().getUserName(),
                                                  mayBeUserInBase.get().getRole());
        JwtAuthResponse jwtAuthResponse =
                new JwtAuthResponse(mayBeUserInBase.get().getUserId(),
                                    mayBeUserInBase.get().getUserName(),
                                    mayBeUserInBase.get().getRole(),
                                    token);
        return jwtAuthResponse;
    }
}

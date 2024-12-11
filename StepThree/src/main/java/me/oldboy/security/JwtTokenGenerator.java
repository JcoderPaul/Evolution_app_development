package me.oldboy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import me.oldboy.core.dto.users.UserReadDto;
import me.oldboy.core.model.database.entity.options.Role;

import javax.crypto.SecretKey;
import java.nio.file.AccessDeniedException;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Class for generate and handling JWT token operations / Класс для генерации и управления JWT токеном.
 */
@RequiredArgsConstructor
public class JwtTokenGenerator {
    private final String secret;

    /**
     * Returns the generated JWT token / Класс возвращающий сгенерированный JWT токен
     *
     * @param accountId the userId used fo generate JWT token / ID пользователя из базы данных для генерации JWT токена
     * @param login the login used fo generate JWT token / логин пользователя применяемый для генерации JWT токена
     * @param role the users Role used fo generate JWT token / роль пользователя используемая для генерации JWT токена
     * @return the generated JWT
     */
    public String getToken(Long accountId, String login, Role role){
        return generateJwtToken(accountId, login, role);
    }

    /**
     * Return username (Login) from private getLogin method
     * Возвращает логин (имя пользователя) из закрытого getLogin метода
     *
     * @param token the JWT from which to extract the claim / Токен из которого будет извлекаться логин
     * @return the extracted login / userName
     */
    public String extractUserName(String token) {
        return getLogin(token);
    }

    /**
     * Generates the secret key for signing JWT / Генерирует секретный ключ для подписи JWT
     *
     * @return the generated secret key / Возвращает сгенерированный ключ
     */
    private SecretKey signKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a JWT for the given UserId, Login (UserName) and Role.
     * Генерирует токен для полученных данных (Имя пользователя (Логин), ID пользователя в БД, Роль (ADMIN или USER))
     *
     * @param userId the userId from DB / ID пользователя из базы данных
     * @param username the login for which to generate the JWT / Логин (UserName) для которого будет сгенерирован токен
     * @param role the Role of user / Роль пользователя указанная при регистрации (ADMIN/USER)
     * @return the generated JWT
     */
    private String generateJwtToken(Long userId, String username, Role role){
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());

        Claims claims = Jwts.claims()
                .subject(username)
                .add("id", userId)
                .add("role", role)
                .build();

        return Jwts.builder()
                .claims(claims)
                .expiration(expirationDate)
                .signWith(signKey())
                .compact();
    }

    /**
     * Extracts username (Login) claim from the given JWT
     * Извлекает логин (имя пользователя) из полученного JWT
     *
     * @param token the JWT from which to extract the claim / Токен из которого будет извлекаться логин
     * @return the extracted claims
     */
    private String getLogin(String token) {
        return Jwts.parser()
                .verifyWith(signKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Authenticates a user based on the given JWT / Аутентифицирует пользователя на основе полученного JWT
     *
     * @param token the JWT to authenticate / токен для аутентификации
     * @param userReadDto user data to authenticate / данные из БД для сравнения с данными из payload ключа
     * @return the authentication user result / если JWT ключ валиден, возвращаем результат аутентификации
     * @throws AccessDeniedException if the JWT is invalid or the user does not exist
     */
    public JwtAuthUser authentication(String token, UserReadDto userReadDto) throws AccessDeniedException{
        if (!isValid(token, userReadDto)) {
            throw new AccessDeniedException("Access denied: Invalid token / Доступ запрещен!");
        }
        return new JwtAuthUser(userReadDto.userName(), userReadDto.role(), true);
    }

    /**
     * Validates the given JWT token / Проверяем JWT ключ на валидность
     *
     * @param token the JWT token to validate / токен для проверки валидности
     * @param userReadDto user data for compare / сведения о пользователе для сравнения со структурой payload
     * @return true if the token is valid, false otherwise
     *         true - токен валиден / false - токен просрочен или данные не совпали
     */
    public boolean isValid(String token, UserReadDto userReadDto) {
        Jws<Claims> claims = Jwts.parser()
                                 .verifyWith(signKey())
                                 .build()
                                 .parseSignedClaims(token);
        final String userName = extractUserName(token);

        return (claims.getPayload().getExpiration().after(new Date()) &&
                userName.equals(userReadDto.userName()));
    }
}
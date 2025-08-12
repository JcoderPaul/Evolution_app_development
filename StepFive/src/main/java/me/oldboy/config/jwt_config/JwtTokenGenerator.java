package me.oldboy.config.jwt_config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Class responsible for handling JWT tokens:
 * - generate a token;
 * - extract the username from a token;
 * - check is a token valid;
 */
@Component
@AllArgsConstructor
@NoArgsConstructor
public class JwtTokenGenerator {

    @Value("${security.jwt.secret}")
    private String jwtSignature;

    /**
     * Get generated token.
     *
     * @param accountId user account id
     * @param login user login for auth
     * @return string representation of generated token
     */
    public String getToken(Long accountId, String login) {
        return generateJwtToken(accountId, login);
    }

    /**
     * Get is a token valid or no
     *
     * @param token string representation of request token
     * @param userDetails convenient presentation of user data
     * @return true - token is valid, false - token not valid
     */
    public boolean isValid(String token, UserDetails userDetails) {
        Jws<Claims> claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token);
        final String userName = extractUserName(token);

        return (claims.getPayload().getExpiration().after(new Date()) &&
                userName.equals(userDetails.getUsername()));
    }

    /**
     * Get user name (login) from request token
     *
     * @param token string representation of request token
     * @return user login (user name)
     */
    public String extractUserName(String token) {
        return getLogin(token);
    }

    /**
     * Get a secret key to verify the signature of the given and received tokens
     *
     * @return secret key for verify
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSignature));
    }

    /**
     * Get user login (name) from token
     *
     * @param token string representation of token
     * @return user login (user name)
     */
    private String getLogin(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Get back generated JWT token
     *
     * @param accountId user account id
     * @param login user login or name
     * @return string representation of generated token
     */
    private String generateJwtToken(Long accountId, String login) {
        Claims claims = Jwts.claims()
                .subject(login)
                .add("id", accountId)
                .build();

        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(Instant.now().plus(24, ChronoUnit.HOURS)))
                .signWith(getKey())
                .compact();
    }
}
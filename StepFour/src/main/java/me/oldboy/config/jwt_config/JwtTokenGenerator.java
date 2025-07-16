package me.oldboy.config.jwt_config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class JwtTokenGenerator {

    @Value("${security.jwt.secret}")
    private String jwtSignature;

    public String getToken(Long accountId, String login) {
        return generateJwtToken(accountId, login);
    }

    public boolean isValid(String token, UserDetails userDetails) {
        Jws<Claims> claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token);
        final String userName = extractUserName(token);

        return (claims.getPayload().getExpiration().after(new Date()) &&
                userName.equals(userDetails.getUsername()));
    }

    public String extractUserName(String token) {
        return getLogin(token);
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSignature));
    }

    private String getLogin(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

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
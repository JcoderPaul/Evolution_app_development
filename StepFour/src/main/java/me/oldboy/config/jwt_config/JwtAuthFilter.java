package me.oldboy.config.jwt_config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.config.security_details.ClientDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private ClientDetailsService clientDetailsService;
    @Autowired
    private JwtTokenGenerator jwtTokenGenerator;
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";

    @Override
    @SneakyThrows
    public void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain filterChain) {

        String authRequestHeader = request.getHeader(HEADER_NAME);
        String tokenFromRequest = null;

        if (authRequestHeader != null && authRequestHeader.startsWith(BEARER_PREFIX)) {
            tokenFromRequest = authRequestHeader.substring(BEARER_PREFIX.length());
        }
        try {
            String accountLogin = jwtTokenGenerator.extractUserName(tokenFromRequest);
            UserDetails userDetails = clientDetailsService.loadUserByUsername(accountLogin);

            if (jwtTokenGenerator.isValid(tokenFromRequest, userDetails)) {
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, userDetails.getUsername(), userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ignored) {
        }
        filterChain.doFilter(request, response);
    }
}
package me.oldboy.config.jwt_config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

/**
 * This class is a filter that executes once per request.
 * It checks if the request has a valid JWT token and sets
 * the authentication in the security context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private final ClientDetailsService clientDetailsService;
    @Autowired
    private final JwtTokenGenerator jwtTokenGenerator;
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";

    /**
     * This method called for every request - check has the request a valid JWT token.
     * If the token is valid - sets the authentication in the security context.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     */
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
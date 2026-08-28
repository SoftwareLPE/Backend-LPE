package com.example.backend_sistema_LPE.apps.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtConfig jwtConfig;
    private final MyUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtConfig jwtConfig, MyUserDetailsService userDetailsService) {
        this.jwtConfig = jwtConfig;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/auth/login".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug(
                    "JWT header missing or invalid format method={} path={}",
                    request.getMethod(),
                    path
            );
            filterChain.doFilter(request, response);
            return;
        }
        final String jwt = authHeader.substring(7); // Quitar "Bearer "

        final String username;
        try {
            username = jwtConfig.extractUsername(jwt);
        } catch (Exception ex) {
            log.warn(
                    "JWT parse failed method={} path={} reason={}",
                    request.getMethod(),
                    path,
                    ex.getMessage()
            );
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtConfig.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info(
                        "JWT authenticated method={} path={} username={} roles={}",
                        request.getMethod(),
                        path,
                        userDetails.getUsername(),
                        userDetails.getAuthorities()
                );
            } else {
                log.warn(
                        "JWT validation failed method={} path={} username={}",
                        request.getMethod(),
                        path,
                        userDetails.getUsername()
                );
            }
        }

        filterChain.doFilter(request, response);
    }

}

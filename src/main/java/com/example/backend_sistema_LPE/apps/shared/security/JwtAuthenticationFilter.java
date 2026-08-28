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
        boolean hasBearerToken = authHeader != null && authHeader.startsWith("Bearer ");
        log.info(
                "JWT filter request method={} path={} hasAuthorizationHeader={} hasBearerToken={}",
                request.getMethod(),
                path,
                authHeader != null,
                hasBearerToken
        );

        if (!hasBearerToken) {
            log.warn("JWT filter skipping authentication because Bearer token is missing for path={}", path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtConfig.extractUsername(jwt);
            log.info("JWT filter extracted username={} for path={}", username, path);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                log.info(
                        "JWT filter loaded user username={} roles={} path={}",
                        userDetails.getUsername(),
                        userDetails.getAuthorities(),
                        path
                );

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
                    log.info("JWT filter populated SecurityContext for username={} path={}", username, path);
                } else {
                    log.warn("JWT filter rejected invalid token for username={} path={}", username, path);
                }
            }
        } catch (Exception ex) {
            log.error(
                    "JWT filter failed to authenticate request method={} path={} error={}",
                    request.getMethod(),
                    path,
                    ex.getMessage(),
                    ex
            );
        }

        filterChain.doFilter(request, response);
    }

}

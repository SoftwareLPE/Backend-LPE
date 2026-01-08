package com.example.backend_sistema_LPE.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtConfig {

    // Debe ser al menos 32 caracteres para HS256
    private static final String SECRET_KEY = "tu_secreto_super_fuerte_que_debe_ir_en_ENV_123456";
    private static final long EXPIRATION_TIME = 1000L * 60 * 60; // 1 hora

    private final SecretKey key;

    public JwtConfig() {
        // Convierte la clave en un SecretKey una sola vez
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date(System.currentTimeMillis());
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("user",userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key, Jwts.SIG.HS256) // Forma correcta en 0.13
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            // Si falla el parseo o la verificación, el token no es válido
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = getClaims(token).getExpiration();
        Date now = new Date(System.currentTimeMillis());
        return expiration.before(now);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)       // Verifica la firma con la misma key
                .build()
                .parseSignedClaims(token)
                .getPayload();          // Obtiene los Claims
    }
}

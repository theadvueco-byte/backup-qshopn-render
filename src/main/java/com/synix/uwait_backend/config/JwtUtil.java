package com.synix.uwait_backend.config;


import com.synix.uwait_backend.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.util.Date;

import static com.synix.uwait_backend.config.JwtConstant.SECRET_KEY;

@Component
public class JwtUtil {

    private static final long EXPIRATION = 24 * 60 * 60 * 1000;
    SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    public String generateToken(User user) {



        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("email", user.getEmail())
                .claim("authorities", "ROLE_" + user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    // Example method in JwtTokenUtil
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .claim("authorities", "ROLE_" + role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

}

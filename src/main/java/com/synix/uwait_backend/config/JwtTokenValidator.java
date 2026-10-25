package com.synix.uwait_backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;

import java.util.List;

public class JwtTokenValidator extends OncePerRequestFilter {
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/auth/login")
                || path.startsWith("/auth/admin/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/auth/forgot-password")
                || path.startsWith("/auth/reset-password")
                || path.startsWith("/api/public")
                || path.startsWith("/shops")
                || path.startsWith("/products")
                || path.startsWith("/reviews")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");

//        return path.startsWith("/auth")
//                || path.startsWith("/api/public")
//                || path.startsWith("/shops")
//                || path.startsWith("/products")
//                || path.startsWith("/reviews")
//                || path.startsWith("/swagger-ui")
//                || path.startsWith("/v3/api-docs")
//                || path.matches("^/[A-Z0-9]+$"); // P1PB2R
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String jwt = extractTokenFromCookies(request);

            if (jwt == null) {
                String header = request.getHeader("Authorization");
                if (header != null && header.startsWith("Bearer ")) {
                    jwt = header.substring(7);
                }
            }

            if (jwt != null) {

                SecretKey key =
                        Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());

                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();

                String email = claims.get("email", String.class);
                String authority = claims.get("authorities", String.class);

                List<GrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority(authority));

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                authorities
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }



        } catch (Exception e) {
            String msg = "Invalid or expired authentication token";
            request.setAttribute("ERROR_MESSAGE", msg);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + msg + "\",\"status\":401}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if ("token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

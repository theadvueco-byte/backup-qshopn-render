package com.synix.uwait_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)

                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // PUBLIC
                        .requestMatchers(
                                "/auth/**",
                                "/api/auth/**",
                                "/api/public/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"

                        ).permitAll()

                        // 🔐 PROTECT THIS
                        .requestMatchers("/auth/me").authenticated()

                        // ADMIN ONLY
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // SHOP / CLIENT ONLY
                        .requestMatchers("/api/owner/**")
                        .hasRole("CLIENT")

                        // AUTHENTICATED
                        .requestMatchers("/api/**")
                        .authenticated()

                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String msg = "Authentication required to access this resource";
                            request.setAttribute("ERROR_MESSAGE", msg);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + msg + "\",\"status\":401}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String msg = "You do not have permission to access this resource";
                            request.setAttribute("ERROR_MESSAGE", msg);
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"" + msg + "\",\"status\":403}");
                        })
                )

                .addFilterBefore(jwtTokenValidator(), BasicAuthenticationFilter.class);

        return http.build();
    }

    // ✅ THIS IS THE KEY FIX
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("JWT based auth only");
        };
    }

    @Bean
    public JwtTokenValidator jwtTokenValidator() {
        return new JwtTokenValidator();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ USE PATTERNS FOR WILDCARDS
        configuration.setAllowedOriginPatterns(List.of(
                "https://*.qshopn.com",
                "https://qshopn.com",
                "http://localhost:5173",
                "http://localhost:3000"
        ));

        configuration.setAllowCredentials(true);

        configuration.setAllowedMethods(List.of(
                "GET","POST","PUT","DELETE","PATCH","OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization","Content-Type","Accept","Origin"
        ));

        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

package com.synix.uwait_backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        filterChain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime;

        String dateTime = LocalDateTime.now().format(formatter);
        String errorMessage = (String) request.getAttribute("ERROR_MESSAGE");

        if (response.getStatus() >= 400 && errorMessage != null) {
            System.out.printf("[%s] %s %d → %s (%d ms)  %s%n",
                    dateTime,
                    request.getMethod(),
                    response.getStatus(),
                    request.getRequestURI(),
                    duration,
                    errorMessage);
        } else {
            System.out.printf("[%s] %s %d  → %s (%d ms)%n",
                    dateTime,
                    request.getMethod(),
                    response.getStatus(),
                    request.getRequestURI(),
                    duration);
        }
    }
}
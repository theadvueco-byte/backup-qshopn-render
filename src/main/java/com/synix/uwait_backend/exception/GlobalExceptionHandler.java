package com.synix.uwait_backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UpgradeRequiredException.class)
    public ResponseEntity<?> handleUpgrade(UpgradeRequiredException ex, HttpServletRequest request) {

        String message = ex.getMessage() != null ? ex.getMessage() : "Upgrade required";

        request.setAttribute("ERROR_MESSAGE", message);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error", "Upgrade Required",
                        "message", message
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex, HttpServletRequest request) {

        String message = ex.getMessage() != null ? ex.getMessage() : "Bad request error";

        request.setAttribute("ERROR_MESSAGE", message);

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "Bad Request",
                        "message", message
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex, HttpServletRequest request) {

        String message = ex.getMessage() != null ? ex.getMessage() : "Unexpected server error";

        request.setAttribute("ERROR_MESSAGE", message);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Something went wrong",
                        "message", message
                ));
    }
}
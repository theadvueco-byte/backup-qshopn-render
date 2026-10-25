package com.synix.uwait_backend.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==========================================
    // 1. Subscription & Business Guard Exceptions
    // ==========================================

    @ExceptionHandler(UpgradeRequiredException.class)
    public ResponseEntity<?> handleUpgrade(UpgradeRequiredException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Upgrade required";
        return buildResponse(HttpStatus.FORBIDDEN, "Upgrade Required", message, request, null);
    }

    @ExceptionHandler(TrialExpiredException.class)
    public ResponseEntity<?> handleTrialExpired(TrialExpiredException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Trial or subscription expired";
        return buildResponse(HttpStatus.FORBIDDEN, "Trial Expired", message, request, null);
    }

    // ==========================================
    // 2. Resource Not Found Exceptions (404)
    // ==========================================

    @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<?> handleNotFound(Exception ex, HttpServletRequest request) {
        String message = ex.getMessage();
        if (message == null || message.isBlank() || message.equalsIgnoreCase("No value present")) {
            message = "Requested resource was not found";
        }
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", message, request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", "Resource not found: " + ex.getResourcePath(), request, null);
    }

    // ==========================================
    // 3. Validation & Malformed Input (400)
    // ==========================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        });

        String primaryMessage = fieldErrors.isEmpty()
                ? "Validation failed"
                : fieldErrors.values().iterator().next();

        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("errors", fieldErrors);

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", primaryMessage, request, extra);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> violations = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            violations.put(violation.getPropertyPath().toString(), violation.getMessage());
        });

        String primaryMessage = violations.isEmpty()
                ? "Constraint violation occurred"
                : violations.values().iterator().next();

        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("errors", violations);

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", primaryMessage, request, extra);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed Request",
                "Malformed JSON request body or unparseable input", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String typeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid type";
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected %s.",
                ex.getValue(), ex.getName(), typeName);
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Parameter", message, request, null);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class,
            MissingServletRequestPartException.class
    })
    public ResponseEntity<?> handleMissingParams(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Missing Parameter", ex.getMessage(), request, null);
    }

    // ==========================================
    // 4. File Upload Limits (413)
    // ==========================================

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSizeException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large",
                "Uploaded file exceeds maximum permitted size", request, null);
    }

    // ==========================================
    // 5. Routing & HTTP Method Restrictions (405, 415)
    // ==========================================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = String.format("HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(), ex.getSupportedHttpMethods());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", message, request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type",
                ex.getMessage(), request, null);
    }

    // ==========================================
    // 6. Security & Authorization Exceptions (401, 403)
    // ==========================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden",
                "You do not have permission to access this resource", request, null);
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<?> handleAuthentication(Exception ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid credentials";
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", message, request, null);
    }

    // ==========================================
    // 7. Database Constraints (409)
    // ==========================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Database constraint violation at {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflict",
                "Operation failed due to database constraint (e.g. duplicate record or foreign key conflict)", request, null);
    }

    // ==========================================
    // 8. General Argument & State Errors (400)
    // ==========================================

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<?> handleIllegalArguments(RuntimeException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request, null);
    }

    // ==========================================
    // 9. Application Runtime Exceptions
    // ==========================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Bad request error";
        String lower = message.toLowerCase();

        // Categorize by common domain messages
        if (lower.contains("not found")) {
            return buildResponse(HttpStatus.NOT_FOUND, "Not Found", message, request, null);
        } else if (lower.contains("unauthorized") || lower.contains("invalid credentials") || lower.contains("not authenticated")) {
            return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", message, request, null);
        } else if (lower.contains("forbidden") || lower.contains("not allowed in your plan") || lower.contains("suspended")) {
            return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", message, request, null);
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message, request, null);
    }

    // ==========================================
    // 10. Fallback Catch-All (500)
    // ==========================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at URI {}: ", request.getRequestURI(), ex);

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected server error occurred. Please try again later.", request, null);
    }

    // ==========================================
    // Helper: Uniform Error Response Builder
    // ==========================================

    private ResponseEntity<?> buildResponse(HttpStatus status, String error, String message,
                                            HttpServletRequest request, Map<String, Object> extra) {
        String msg = (message != null && !message.isBlank()) ? message : error;

        if (request != null) {
            request.setAttribute("ERROR_MESSAGE", msg);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("message", msg);
        body.put("status", status.value());
        body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (request != null) {
            body.put("path", request.getRequestURI());
        }
        if (extra != null && !extra.isEmpty()) {
            body.putAll(extra);
        }

        return ResponseEntity.status(status).body(body);
    }
}
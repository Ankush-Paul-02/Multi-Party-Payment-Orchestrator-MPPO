package com.example.user_service.exceptions;

import com.example.user_service.business.dto.DefaultResponseDto;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserInfoException.class)
    public ResponseEntity<DefaultResponseDto<Map<String, Object>>> handleUserInfoException(UserInfoException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new DefaultResponseDto<Map<String, Object>>(
                        DefaultResponseDto.Status.FAILED,
                        Map.of(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DefaultResponseDto<Map<String, Object>>> handleValidationException(MethodArgumentNotValidException ex) {

        // Extract validation errors to Map<String, String>
        var stringErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(
                        Collectors.toMap(
                                FieldError::getField,
                                DefaultMessageSourceResolvable::getDefaultMessage,
                                (a, b) -> b
                        )
                );

        // Convert to Map<String, Object> (required by DTO)
        Map<String, Object> errorMap = new HashMap<>(stringErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new DefaultResponseDto<Map<String, Object>>(
                        DefaultResponseDto.Status.FAILED,
                        errorMap,
                        "Validation failed"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DefaultResponseDto<Map<String, Object>>> handleGenericException(Exception ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new DefaultResponseDto<Map<String, Object>>(
                        DefaultResponseDto.Status.FAILED,
                        Map.of(),
                        ex.getMessage()
                ));
    }
}

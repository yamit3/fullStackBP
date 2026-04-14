package com.pichincha.software.engineer.back.configuration;

import com.pichincha.software.engineer.back.exception.ApplicationException;
import com.pichincha.software.engineer.back.service.dto.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionConfiguration {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Object> handleClienteNotFoundException( ApplicationException ex, HttpServletRequest request) {

        return new ResponseEntity<>(ErrorDto.builder()
                .status(ex.getStatus().value())
                .message(ex.getMessage())
                .build(), ex.getStatus());
    }


}

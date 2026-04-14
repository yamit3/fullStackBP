package com.pichincha.software.engineer.back.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {

    private final HttpStatus status;
    private final String message;


    public ApplicationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.message = message;
    }
}

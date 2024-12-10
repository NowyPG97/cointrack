package com.app.cointrack.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestValidationException extends RuntimeException {
    private String message;
}

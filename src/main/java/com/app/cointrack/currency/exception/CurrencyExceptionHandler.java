package com.app.cointrack.currency.exception;

import com.app.cointrack.common.exception.ExternalApiCommunicationException;
import com.app.cointrack.common.exception.RequestValidationException;
import com.app.cointrack.common.response.ExceptionResponse;
import com.app.cointrack.common.response.FieldValidationFailedResponse;
import com.app.cointrack.currency.controller.CurrencyController;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice(assignableTypes = {CurrencyController.class})
public class CurrencyExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {ExternalApiCommunicationException.class })
    protected ExceptionResponse externalApiCommunicationException(ExternalApiCommunicationException ex) {
        return ExceptionResponse.builder()
                .message(ex.getMessage())
                .errorStatus(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .build();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {RequestValidationException.class })
    protected ExceptionResponse requestValidationException(RequestValidationException ex) {
        return ExceptionResponse.builder()
                .message(ex.getMessage())
                .errorStatus(HttpStatus.BAD_REQUEST.name())
                .build();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {MethodArgumentNotValidException.class })
    protected ExceptionResponse methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();
        Set<FieldValidationFailedResponse> fieldValidationFailedResponses = errors.stream().map(e -> FieldValidationFailedResponse.builder()
                .message(e.getDefaultMessage())
                .field(e.getField())
                .build()).collect(Collectors.toSet());
        return ExceptionResponse.builder()
                .fieldsValidationResults(fieldValidationFailedResponses)
                .message(ex.getBody().getDetail())
                .errorStatus(HttpStatus.BAD_REQUEST.name())
                .build();
    }
}

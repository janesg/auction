package com.devxpress.auction.controller;

import com.devxpress.auction.api.ApiError;
import com.devxpress.auction.api.exception.BaseException;
import com.devxpress.auction.api.exception.InvalidResourceException;
import com.devxpress.auction.api.exception.ResourceCrudException;
import com.devxpress.auction.api.exception.ResourceNotFoundException;
import com.devxpress.auction.api.exception.StaleResourceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.devxpress.auction.api.ApiErrorMessage.INVALID_RESOURCE_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.MISSING_OR_INVALID_ARGUMENT_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.RESOURCE_NOT_FOUND_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.RESOURCE_OPERATION_ERROR_MSG;
import static com.devxpress.auction.api.ApiErrorMessage.SYSTEM_ERROR_MSG;

@ControllerAdvice("com.devxpress.auction.controller")
public class ControllerExceptionHandler {

    @ExceptionHandler({
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<Object> handleIllegalArgumentException(Throwable ex) {
        ApiError error = ApiError.ApiErrorBuilder
                .createInstance(HttpStatus.BAD_REQUEST)
                .withMessage(MISSING_OR_INVALID_ARGUMENT_MSG)
                .withContextDetail(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidResourceException.class)
    public ResponseEntity<Object> handleInvalidResourceException(InvalidResourceException ex) {
        ApiError.ApiErrorBuilder builder = ApiError.ApiErrorBuilder
                .createInstance(HttpStatus.UNPROCESSABLE_ENTITY)
                .withMessage(INVALID_RESOURCE_MSG)
                .withCode(ex.getErrorCode());

        if (!ex.getReasons().isEmpty()) {
            ex.getReasons().forEach(builder::withContextDetail);
        } else {
            builder.withContextDetail(ex.getMessage());
        }

        return new ResponseEntity<>(builder.build(), new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(StaleResourceException.class)
    public ResponseEntity<Object> handleStaleResourceException(StaleResourceException ex) {
        ApiError.ApiErrorBuilder builder = ApiError.ApiErrorBuilder
                .createInstance(HttpStatus.UNPROCESSABLE_ENTITY)
                .withMessage(INVALID_RESOURCE_MSG)
                .withCode(ex.getErrorCode())
                .withContextDetail(ex.getMessage());

        return new ResponseEntity<>(builder.build(), new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ResourceCrudException.class)
    public ResponseEntity<Object> handleResourceCrudException(ResourceCrudException ex) {
        ApiError error = ApiError.ApiErrorBuilder
                .createInstance(HttpStatus.CONFLICT)
                .withMessage(RESOURCE_OPERATION_ERROR_MSG)
                .withCode(ex.getErrorCode())
                .withContextDetail(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiError error = ApiError.ApiErrorBuilder
                .createInstance(HttpStatus.NOT_FOUND)
                .withMessage(RESOURCE_NOT_FOUND_MSG)
                .withCode(ex.getErrorCode())
                .withContextDetail(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Object> handleBaseException(BaseException ex) {
        ApiError error = ApiError.ApiErrorBuilder
                .createInstance(HttpStatus.INTERNAL_SERVER_ERROR)
                .withMessage(SYSTEM_ERROR_MSG)
                .withCode(ex.getErrorCode())
                .withContextDetail(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        ApiError error = ApiError.ApiErrorBuilder
                .createInstance(HttpStatus.INTERNAL_SERVER_ERROR)
                .withMessage(SYSTEM_ERROR_MSG)
                .withContextDetail(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

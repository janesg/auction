package com.devxpress.auction.api.exception;

/**
 * Exception to be thrown when a resource which is the target of an API operation is not found.
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(Integer errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Integer errorCode) {
        super(message, errorCode);
    }

}

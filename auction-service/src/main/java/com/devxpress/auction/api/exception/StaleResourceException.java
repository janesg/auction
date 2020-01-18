package com.devxpress.auction.api.exception;

/**
 * Exception to be thrown when the version of a resource to be mutated does not match that in persistent storage
 */
public class StaleResourceException extends BaseException {

    public StaleResourceException(String exceptionMessage) { super(exceptionMessage); }

    public StaleResourceException(String exceptionMessage, Integer errorCode) { super(exceptionMessage, errorCode); }
}

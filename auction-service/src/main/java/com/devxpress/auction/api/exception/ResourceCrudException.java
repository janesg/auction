package com.devxpress.auction.api.exception;

public class ResourceCrudException extends BaseException {

    public ResourceCrudException(String exceptionMessage) {
        super(exceptionMessage);
    }

    public ResourceCrudException(String exceptionMessage, Integer errorCode) {
        super(exceptionMessage, errorCode);
    }
}

package com.devxpress.auction.api.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private Integer errorCode;

    public BaseException() {
        super();
    }

    public BaseException(Integer errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public BaseException(String exceptionMessage) {
        super(exceptionMessage);
    }

    public BaseException(String exceptionMessage, Integer errorCode) {
        super(exceptionMessage);
        this.errorCode = errorCode;
    }

}

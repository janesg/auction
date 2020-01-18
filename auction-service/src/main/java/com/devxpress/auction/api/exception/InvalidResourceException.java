package com.devxpress.auction.api.exception;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class InvalidResourceException extends BaseException {

    private Set<String> reasons = new LinkedHashSet<>();

    public InvalidResourceException() {
        super();
    }

    public InvalidResourceException(Integer errorCode) {
        super(errorCode);
    }

    public InvalidResourceException(String exceptionMessage) {
        super(exceptionMessage);
    }

    public InvalidResourceException(String exceptionMessage, Integer errorCode) {
        super(exceptionMessage, errorCode);
    }

    public void addReason(String reason) {
        reasons.add(reason);
    }

    public Set<String> getReasons() {
        return Collections.unmodifiableSet(reasons);
    }
}

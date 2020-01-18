package com.devxpress.auction.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtils {

    public static String getMessage(Throwable t) {
        String message = t.getMessage();

        if (message == null || message.isEmpty()) {
            if (t.getCause() != null && t.getCause().getMessage() != null && !t.getCause().getMessage().isEmpty()) {
                message = t.getCause().getMessage();
            } else {
                StringWriter exceptionStack = new StringWriter();
                t.printStackTrace(new PrintWriter(exceptionStack));
                message = exceptionStack.toString();
            }
        }

        return message;
    }
}
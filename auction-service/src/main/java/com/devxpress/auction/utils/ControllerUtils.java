package com.devxpress.auction.utils;

public final class ControllerUtils {

    private ControllerUtils() {
    }

    public static long convertStringToResourceId(String idStr, String emptyMessage, String nonNumericMessage) {

        if (idStr == null || idStr.trim().length() == 0) {
            throw new IllegalArgumentException(emptyMessage);
        }

        try {
            return Long.parseLong(idStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nonNumericMessage);
        }
    }
}

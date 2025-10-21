package com.adaptris.core;

import java.util.Arrays;

public record ExceptionDetails(
            String exception,
            String message,
            String cause,
            String stacktrace,
            Class<? extends Exception> exceptionClass) {

    public static ExceptionDetails from(Exception exception) {
        return new ExceptionDetails(
                (exception != null ? exception.toString() : null),
                (exception != null ? exception.getMessage() : null),
                null,
                (exception != null ? Arrays.toString(exception.getStackTrace()) : null),
                (exception != null ? exception.getClass() : null));
    }

    public static ExceptionDetails from(Exception exception, String cause) {
        return new ExceptionDetails(
                (exception != null ? exception.toString() : null),
                (exception != null ? exception.getMessage() : null),
                (cause),
                (exception != null ? Arrays.toString(exception.getStackTrace()) : null),
                (exception != null ? exception.getClass() : null));
    }

}

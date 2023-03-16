package com.server.exception;

public class CsvException extends Exception {

    public CsvException(String message) {
        super(message);
    }
    public CsvException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

package ru.practicum.shareit.error;

public class EmailException extends RuntimeException {
    public EmailException(String message) {
        super(message);
    }
}


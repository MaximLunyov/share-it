package ru.practicum.shareit.error.model;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

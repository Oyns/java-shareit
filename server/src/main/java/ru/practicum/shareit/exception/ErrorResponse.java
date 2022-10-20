package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final String error;

    public ErrorResponse(String message) {
        this.error = message;
    }
}
package ar.edu.unq.flights.controller.dto;

import java.time.LocalDateTime;

public record ErrorDTO(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
    public ErrorDTO(int status, String error, String message) {
        this(status, error, message, LocalDateTime.now());
    }
}

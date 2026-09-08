package ar.edu.unq.flights.controller.dto;

import ar.edu.unq.flights.model.Passenger;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Data Transfer Object representing a passenger.")
public record PassengerDTO(
        int dni,
        String name,
        String surname
) {
    public static PassengerDTO from(Passenger passenger) {
        return new PassengerDTO(
                passenger.getDni(),
                passenger.getName(),
                passenger.getSurname()
        );
    }
}

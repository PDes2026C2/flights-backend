package ar.edu.unq.flights.controller.dto;

import ar.edu.unq.flights.model.Passenger;

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

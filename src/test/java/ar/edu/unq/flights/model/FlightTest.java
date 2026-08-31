package ar.edu.unq.flights.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static ar.edu.unq.flights.builder.CityBuilder.aCity;
import static ar.edu.unq.flights.builder.FlightBuilder.aFlight;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FlightTest {

    @Test
    void createFlight_withValidData_shouldSucceed() {
        City buenosAires = aCity().withId(1L).build();
        City madrid = aCity().withId(2L).build();

        Flight flight = aFlight()
                .withCapacity(200)
                .withAirline("Aerolíneas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build();

        Assertions.assertNotNull(flight);
        assertEquals(200, flight.getCapacity());
        assertEquals(buenosAires, flight.getOriginCity());
        assertEquals(madrid, flight.getDestinationCity());
    }

    @Test
    void createFlight_withSameOriginAndDestination_shouldThrowException() {
        City buenosAires = aCity().withId(1L).build();

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new Flight(200, "Iberia", LocalDateTime.now(), LocalDateTime.now().plusHours(5), buenosAires, buenosAires)
        );

        assertEquals("El origen y el destino no pueden ser la misma ciudad", exception.getMessage());
    }

    @Test
    void createFlight_withArrivalBeforeDeparture_shouldThrowException() {
        City buenosAires = aCity().withId(1L).build();
        City madrid = aCity().withId(2L).build();

        LocalDateTime departure = LocalDateTime.of(2026, 9, 1, 10, 0);
        LocalDateTime invalidArrival = departure.minusHours(2);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new Flight(200, "Iberia", departure, invalidArrival, buenosAires, madrid)
        );

        assertEquals("La fecha de llegada debe ser posterior a la fecha de salida", exception.getMessage());
    }

    @Test
    void createFlight_withZeroOrNegativeCapacity_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Flight(0, "Iberia", LocalDateTime.now(), LocalDateTime.now().plusHours(5), aCity().build(), aCity().build())
        );
    }
}

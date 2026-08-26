package ar.edu.unq.flights.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FlightTest {
    private City buenosAires;
    private City madrid;
    private LocalDateTime departure;
    private LocalDateTime arrival;

    @BeforeEach
    void setUp() {
        Country ar = new Country("AR", "Argentina");
        Country es = new Country("ES", "España");

        buenosAires = new City("Buenos Aires", ar);
        buenosAires.setId(1L);

        madrid = new City("Madrid", es);
        madrid.setId(2L);

        departure = LocalDateTime.of(2026, 9, 1, 10, 0);
        arrival = LocalDateTime.of(2026, 9, 1, 22, 0);
    }

    @Test
    void createFlight_withValidData_shouldSucceed() {
        Flight flight = new Flight(200, "Aerolíneas Argentinas", departure, arrival, buenosAires, madrid);

        Assertions.assertNotNull(flight);
        assertEquals(200, flight.getCapacity());
        assertEquals(buenosAires, flight.getOriginCity());
        assertEquals(madrid, flight.getDestinationCity());
    }

    @Test
    void createFlight_withSameOriginAndDestination_shouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new Flight(200, "Iberia", departure, arrival, buenosAires, buenosAires)
        );

        assertEquals("El origen y el destino no pueden ser la misma ciudad", exception.getMessage());
    }

    @Test
    void createFlight_withArrivalBeforeDeparture_shouldThrowException() {
        LocalDateTime invalidArrival = departure.minusHours(2);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                new Flight(200, "Iberia", departure, invalidArrival, buenosAires, madrid)
        );

        assertEquals("La fecha de llegada debe ser posterior a la fecha de salida", exception.getMessage());
    }

    @Test
    void createFlight_withZeroOrNegativeCapacity_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Flight(0, "Iberia", departure, arrival, buenosAires, madrid)
        );
    }
}

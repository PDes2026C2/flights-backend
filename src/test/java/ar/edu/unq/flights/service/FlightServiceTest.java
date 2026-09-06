package ar.edu.unq.flights.service;

import ar.edu.unq.flights.controller.dto.FlightFilterDTO;
import ar.edu.unq.flights.exception.FlightFullException;
import ar.edu.unq.flights.model.City;
import ar.edu.unq.flights.model.Country;
import ar.edu.unq.flights.model.Flight;
import ar.edu.unq.flights.model.Passenger;
import ar.edu.unq.flights.repository.CityRepository;
import ar.edu.unq.flights.repository.CountryRepository;
import ar.edu.unq.flights.repository.FlightRepository;
import ar.edu.unq.flights.repository.PassengerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static ar.edu.unq.flights.builder.CityBuilder.aCity;
import static ar.edu.unq.flights.builder.CountryBuilder.aCountry;
import static ar.edu.unq.flights.builder.FlightBuilder.aFlight;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class FlightServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Test
    @DisplayName("Should search flights by airline correctly")
    void searchFlightsByAirline() {

        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).build());

        flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        flightRepository.save(aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .build());

        FlightFilterDTO filter = new FlightFilterDTO(
                "Aerolineas",
                null, null,
                null, null,
                null, null,
                null, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        List<Flight> result = flightService.searchFlights(filter, pageable);

        assertEquals(1, result.size());
        assertEquals("Aerolineas Argentinas", result.getFirst().getAirline());
        assertEquals("AR", result.getFirst().getOriginCity().getCountry().getIsoCode());
    }

    @Test
    @DisplayName("Should search flights by origin country ISO correctly")
    void searchFlightsByOriginCity() {

        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).withName("Madrid").build());

        flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        Flight madridToIberiaFlight = aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .build();

        flightRepository.save(madridToIberiaFlight);

        FlightFilterDTO filter = new FlightFilterDTO(
                null,
                null, null,
                null, null,
                madrid.getId(), null,
                null, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        List<Flight> result = flightService.searchFlights(filter, pageable);

        assertEquals(1, result.size());
        assertEquals("Madrid", result.getFirst().getOriginCity().getName());
    }

    @Test
    @DisplayName("Should search flights by origin country ISO correctly")
    void searchFlightsByOriginCountryIso() {

        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).build());

        flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        flightRepository.save(aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .build());

        FlightFilterDTO filter = new FlightFilterDTO(
                null,
                null, null,
                null, null,
                null, "AR",
                null, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        List<Flight> result = flightService.searchFlights(filter, pageable);

        assertEquals(1, result.size());
        assertEquals("AR", result.getFirst().getOriginCity().getCountry().getIsoCode());
    }

    @Test
    @DisplayName("Should search flights by destination city correctly")
    void searchFlightsByDestinationCity() {

        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).withName("Madrid").build());

        flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        flightRepository.save(aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .build());

        FlightFilterDTO filter = new FlightFilterDTO(
                null,
                null, null,
                null, null,
                null, null,
                madrid.getId(), null
        );

        Pageable pageable = PageRequest.of(0, 10);

        List<Flight> result = flightService.searchFlights(filter, pageable);

        assertEquals(1, result.size());
        assertEquals("Madrid", result.getFirst().getDestinationCity().getName());
    }

    @Test
    @DisplayName("Should search flights by destination country ISO correctly")
    void searchFlightsByDestinationCountryIso() {

        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).build());

        flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        flightRepository.save(aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .build());

        FlightFilterDTO filter = new FlightFilterDTO(
                null,
                null, null,
                null, null,
                null, null,
                null, "AR"
        );
        Pageable pageable = PageRequest.of(0, 10);

        List<Flight> result = flightService.searchFlights(filter, pageable);

        assertEquals(1, result.size());
        assertEquals("AR", result.getFirst().getDestinationCity().getCountry().getIsoCode());
    }


    @Test
    @DisplayName("Should return all flights when filter is empty")
    void searchFlightsEmptyFilter() {

        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).build());

        flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        flightRepository.save(aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .build());

        FlightFilterDTO filter = new FlightFilterDTO(
                null, null, null, null, null, null, null, null, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        List<Flight> result = flightService.searchFlights(filter, pageable);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should sell flight to an existing passenger, verifying passenger is present and added to flight when retrieved by id")
    void sellFlight_withExistingPassenger_shouldAddPassengerToFlight() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).build());

        Flight flight = flightRepository.save(aFlight()
                .withCapacity(10)
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        Passenger existingPassenger = passengerRepository.save(new Passenger(12345678, "Juan", "Perez"));

        flightService.sellFlight(flight.getId(), existingPassenger.getDni(), "Juan", "Perez");

        Flight updatedFlight = flightRepository.findById(flight.getId()).orElseThrow();
        assertEquals(1, updatedFlight.getPassengers().size());
        assertEquals(existingPassenger.getDni(), updatedFlight.getPassengers().getFirst().getDni());
        assertEquals("Juan", updatedFlight.getPassengers().getFirst().getName());
        assertEquals("Perez", updatedFlight.getPassengers().getFirst().getSurname());
        assertTrue(passengerRepository.findById(existingPassenger.getDni()).isPresent());
    }

    @Test
    @DisplayName("Should sell flight to a non-existing passenger, creating the passenger and adding it to the flight when retrieved by id")
    void sellFlight_withNonExistingPassenger_shouldCreatePassengerAndAddToFlight() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).build());

        Flight flight = flightRepository.save(aFlight()
                .withCapacity(10)
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        int newPassengerDni = 87654321;
        assertFalse(passengerRepository.findById(newPassengerDni).isPresent());

        flightService.sellFlight(flight.getId(), newPassengerDni, "Maria", "Gomez");

        Flight updatedFlight = flightRepository.findById(flight.getId()).orElseThrow();
        assertEquals(1, updatedFlight.getPassengers().size());
        assertEquals(newPassengerDni, updatedFlight.getPassengers().getFirst().getDni());
        assertEquals("Maria", updatedFlight.getPassengers().getFirst().getName());
        assertEquals("Gomez", updatedFlight.getPassengers().getFirst().getSurname());
        assertTrue(passengerRepository.findById(newPassengerDni).isPresent());
    }

    @Test
    @DisplayName("Should successfully sell flight when flight has capacity of 1")
    void sellFlight_withCapacityOne_shouldSucceed() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).build());

        Flight flight = flightRepository.save(aFlight()
                .withCapacity(1)
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        flightService.sellFlight(flight.getId(), 11223344, "Carlos", "Lopez");

        Flight updatedFlight = flightRepository.findById(flight.getId()).orElseThrow();
        assertEquals(1, updatedFlight.getPassengers().size());
        assertEquals(11223344, updatedFlight.getPassengers().getFirst().getDni());
        assertEquals("Carlos", updatedFlight.getPassengers().getFirst().getName());
    }

    @Test
    @DisplayName("Should throw FlightFullException when selling ticket on a flight with 0 remaining capacity")
    void sellFlight_withZeroRemainingCapacity_shouldThrowFlightFullException() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).build());

        Flight flight = flightRepository.save(aFlight()
                .withCapacity(1)
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        // Sell the 1 available ticket
        flightService.sellFlight(flight.getId(), 11111111, "Ana", "Ruiz");

        // Attempting to sell another ticket on full flight must fail
        assertThrows(FlightFullException.class, () ->
                flightService.sellFlight(flight.getId(), 22222222, "Pedro", "Sanchez")
        );
    }
}

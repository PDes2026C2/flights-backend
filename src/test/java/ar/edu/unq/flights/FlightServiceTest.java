package ar.edu.unq.flights;

import ar.edu.unq.flights.controller.FlightFilterDTO;
import ar.edu.unq.flights.model.City;
import ar.edu.unq.flights.model.Country;
import ar.edu.unq.flights.model.Flight;
import ar.edu.unq.flights.repository.CityRepository;
import ar.edu.unq.flights.repository.CountryRepository;
import ar.edu.unq.flights.repository.FlightRepository;
import ar.edu.unq.flights.service.FlightService;
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
import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAirline()).isEqualTo("Aerolineas Argentinas");
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

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOriginCity().getCountry().getIsoCode()).isEqualTo("AR");
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

        assertThat(result).hasSize(2);
    }
}

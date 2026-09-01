package ar.edu.unq.flights.controller;

import ar.edu.unq.flights.model.City;
import ar.edu.unq.flights.model.Country;
import ar.edu.unq.flights.model.Flight;
import ar.edu.unq.flights.repository.CityRepository;
import ar.edu.unq.flights.repository.CountryRepository;
import ar.edu.unq.flights.repository.FlightRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static ar.edu.unq.flights.builder.CityBuilder.aCity;
import static ar.edu.unq.flights.builder.CountryBuilder.aCountry;
import static ar.edu.unq.flights.builder.FlightBuilder.aFlight;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class FlightControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Test
    @DisplayName("Should search flights by airline and return matching FlightDTOs")
    void searchFlightsByAirline() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").withName("España").build());

        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withName("Madrid").withCountry(spain).build());

        Flight flight1 = flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        flightRepository.save(aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .build());

        mockMvc.perform(get("/flights")
                        .param("airline", "Aerolineas")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(flight1.getId()))
                .andExpect(jsonPath("$[0].airline").value("Aerolineas Argentinas"))
                .andExpect(jsonPath("$[0].originCity.id").value(buenosAires.getId()))
                .andExpect(jsonPath("$[0].originCity.name").value("Buenos Aires"))
                .andExpect(jsonPath("$[0].originCity.country.isoCode").value("AR"))
                .andExpect(jsonPath("$[0].originCity.country.name").value("Argentina"))
                .andExpect(jsonPath("$[0].destinationCity.id").value(madrid.getId()))
                .andExpect(jsonPath("$[0].destinationCity.name").value("Madrid"))
                .andExpect(jsonPath("$[0].destinationCity.country.isoCode").value("ES"))
                .andExpect(jsonPath("$[0].destinationCity.country.name").value("España"))
                .andExpect(jsonPath("$[0].departureDate").isNotEmpty())
                .andExpect(jsonPath("$[0].arrivalDate").isNotEmpty());
    }

    @Test
    @DisplayName("Should search flights by origin city ID and return matching FlightDTOs")
    void searchFlightsByOriginCity() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).withName("Madrid").build());

        flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        Flight iberiaFlight = flightRepository.save(aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .build());

        mockMvc.perform(get("/flights")
                        .param("originCityId", madrid.getId().toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(iberiaFlight.getId()))
                .andExpect(jsonPath("$[0].airline").value("Iberia"))
                .andExpect(jsonPath("$[0].originCity.id").value(madrid.getId()))
                .andExpect(jsonPath("$[0].originCity.name").value("Madrid"));
    }

    @Test
    @DisplayName("Should search flights by origin country ISO code and return matching FlightDTOs")
    void searchFlightsByOriginCountryIso() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).build());

        Flight aerolineasFlight = flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        flightRepository.save(aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .build());

        mockMvc.perform(get("/flights")
                        .param("originCountryIsoCode", "AR")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(aerolineasFlight.getId()))
                .andExpect(jsonPath("$[0].airline").value("Aerolineas Argentinas"))
                .andExpect(jsonPath("$[0].originCity.country.isoCode").value("AR"));
    }

    @Test
    @DisplayName("Should search flights by destination city ID and return matching FlightDTOs")
    void searchFlightsByDestinationCity() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).withName("Madrid").build());

        Flight aerolineasFlight = flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        flightRepository.save(aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .build());

        mockMvc.perform(get("/flights")
                        .param("destinationCityId", madrid.getId().toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(aerolineasFlight.getId()))
                .andExpect(jsonPath("$[0].airline").value("Aerolineas Argentinas"))
                .andExpect(jsonPath("$[0].destinationCity.id").value(madrid.getId()))
                .andExpect(jsonPath("$[0].destinationCity.name").value("Madrid"));
    }

    @Test
    @DisplayName("Should search flights by destination country ISO code and return matching FlightDTOs")
    void searchFlightsByDestinationCountryIso() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).build());

        flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .build());

        Flight iberiaFlight = flightRepository.save(aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .build());

        mockMvc.perform(get("/flights")
                        .param("destinationCountryIsoCode", "AR")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(iberiaFlight.getId()))
                .andExpect(jsonPath("$[0].airline").value("Iberia"))
                .andExpect(jsonPath("$[0].destinationCity.country.isoCode").value("AR"));
    }

    @Test
    @DisplayName("Should return all flights when filter is empty")
    void searchFlightsEmptyFilter() throws Exception {
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

        mockMvc.perform(get("/flights")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].airline", containsInAnyOrder("Aerolineas Argentinas", "Iberia")));
    }

    @Test
    @DisplayName("Should search flights by departure date range correctly")
    void searchFlightsByDepartureDateRange() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).build());

        LocalDateTime now = LocalDateTime.now().withNano(0);

        Flight flight1 = flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .withDepartureDate(now.plusDays(2))
                .withArrivalDate(now.plusDays(2).plusHours(12))
                .build());

        flightRepository.save(aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .withDepartureDate(now.plusDays(10))
                .withArrivalDate(now.plusDays(10).plusHours(12))
                .build());

        mockMvc.perform(get("/flights")
                        .param("departureDateFrom", now.plusDays(1).toString())
                        .param("departureDateTo", now.plusDays(3).toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(flight1.getId()))
                .andExpect(jsonPath("$[0].airline").value("Aerolineas Argentinas"));
    }

    @Test
    @DisplayName("Should search flights by arrival date range correctly")
    void searchFlightsByArrivalDateRange() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").build());

        City buenosAires = cityRepository.save(aCity().withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withCountry(spain).build());

        LocalDateTime now = LocalDateTime.now().withNano(0);

        flightRepository.save(aFlight()
                .withAirline("Aerolineas Argentinas")
                .withOriginCity(buenosAires)
                .withDestinationCity(madrid)
                .withDepartureDate(now.plusDays(2))
                .withArrivalDate(now.plusDays(2).plusHours(12))
                .build());

        Flight flight2 = flightRepository.save(aFlight()
                .withAirline("Iberia")
                .withOriginCity(madrid)
                .withDestinationCity(buenosAires)
                .withDepartureDate(now.plusDays(10))
                .withArrivalDate(now.plusDays(10).plusHours(12))
                .build());

        mockMvc.perform(get("/flights")
                        .param("arrivalDateFrom", now.plusDays(9).toString())
                        .param("arrivalDateTo", now.plusDays(11).toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(flight2.getId()))
                .andExpect(jsonPath("$[0].airline").value("Iberia"));
    }

    @Test
    @DisplayName("Should apply pagination parameters correctly")
    void searchFlightsWithPagination() throws Exception {
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

        mockMvc.perform(get("/flights")
                        .param("page", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}

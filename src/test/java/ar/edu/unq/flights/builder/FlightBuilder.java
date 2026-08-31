package ar.edu.unq.flights.builder;

import ar.edu.unq.flights.model.City;
import ar.edu.unq.flights.model.Flight;

import java.time.LocalDateTime;

import static ar.edu.unq.flights.builder.CityBuilder.aCity;
import static ar.edu.unq.flights.builder.CountryBuilder.aCountry;

public class FlightBuilder {
    private Long id = null;
    private int capacity = 200;
    private String airline = "Aerolineas Argentinas";
    private LocalDateTime departureDate = LocalDateTime.now().plusDays(1);
    private LocalDateTime arrivalDate = LocalDateTime.now().plusDays(1).plusHours(12);
    private City originCity = aCity().withName("Buenos Aires").withCountry(aCountry().withIsoCode("AR").withName("Argentina").build()).build();
    private City destinationCity = aCity().withName("Madrid").withCountry(aCountry().withIsoCode("ES").withName("España").build()).build();

    public static FlightBuilder aFlight() {
        return new FlightBuilder();
    }

    public FlightBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public FlightBuilder withCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public FlightBuilder withAirline(String airline) {
        this.airline = airline;
        return this;
    }

    public FlightBuilder withDepartureDate(LocalDateTime departureDate) {
        this.departureDate = departureDate;
        return this;
    }

    public FlightBuilder withArrivalDate(LocalDateTime arrivalDate) {
        this.arrivalDate = arrivalDate;
        return this;
    }

    public FlightBuilder withOriginCity(City originCity) {
        this.originCity = originCity;
        return this;
    }

    public FlightBuilder withDestinationCity(City destinationCity) {
        this.destinationCity = destinationCity;
        return this;
    }

    public Flight build() {
        Flight flight = new Flight(capacity, airline, departureDate, arrivalDate, originCity, destinationCity);
        flight.setId(id);
        return flight;
    }
}

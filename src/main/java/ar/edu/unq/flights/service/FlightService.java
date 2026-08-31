package ar.edu.unq.flights.service;

import ar.edu.unq.flights.controller.FlightFilterDTO;
import ar.edu.unq.flights.model.Flight;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FlightService {
    List<Flight> searchFlights(FlightFilterDTO filter, Pageable page);
}

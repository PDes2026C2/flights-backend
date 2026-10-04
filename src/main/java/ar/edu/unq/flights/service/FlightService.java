package ar.edu.unq.flights.service;

import ar.edu.unq.flights.controller.dto.FlightFilterDTO;
import ar.edu.unq.flights.model.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlightService {
    Page<Flight> searchFlights(FlightFilterDTO filter, Pageable page);
    Flight sellFlight(Long flightId, int passengerDni, String passengerName, String passengerSurname);
    Flight cancelFlight(Long flightId, int passengerDni, String passengerName, String passengerSurname);
    Flight getFlightById(Long flightId);
}

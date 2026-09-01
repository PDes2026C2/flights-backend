package ar.edu.unq.flights.controller;

import ar.edu.unq.flights.controller.dto.FlightDTO;
import ar.edu.unq.flights.controller.dto.FlightFilterDTO;
import ar.edu.unq.flights.model.Flight;
import ar.edu.unq.flights.service.FlightService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/flights")
public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    public ResponseEntity<List<FlightDTO>> searchFlights(FlightFilterDTO filter, Pageable page) {
        List<Flight> flights = flightService.searchFlights(filter, page);
        return ResponseEntity.ok(flights.stream().map(FlightDTO::from).toList());
    }
}


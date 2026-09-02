package ar.edu.unq.flights.service.impl;

import ar.edu.unq.flights.controller.dto.FlightFilterDTO;
import ar.edu.unq.flights.exception.FlightNotFoundException;
import ar.edu.unq.flights.model.Flight;
import ar.edu.unq.flights.model.Passenger;
import ar.edu.unq.flights.repository.FlightRepository;
import ar.edu.unq.flights.repository.specifications.FlightSpecifications;
import ar.edu.unq.flights.service.FlightService;
import ar.edu.unq.flights.service.PassengerService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlightServiceImpl implements FlightService {
    private final FlightRepository flightRepository;
    private final PassengerService passengerService;

    public FlightServiceImpl(FlightRepository flightRepository, PassengerService passengerService) {
        this.flightRepository = flightRepository;
        this.passengerService = passengerService;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Flight> searchFlights(FlightFilterDTO filter, Pageable page) {
        Specification<Flight> spec = Specification
                .where(FlightSpecifications.hasAirline(filter.airline()))
                .and(FlightSpecifications.departureDateBetween(filter.departureDateFrom(), filter.departureDateTo()))
                .and(FlightSpecifications.arrivalDateBetween(filter.arrivalDateFrom(), filter.arrivalDateTo()))
                .and(FlightSpecifications.hasOriginCity( filter.originCityId()))
                .and(FlightSpecifications.hasOriginCountryIso(filter.originCountryIsoCode()))
                .and(FlightSpecifications.hasDestinationCity(filter.destinationCityId()))
                .and(FlightSpecifications.hasDestinationCountryIso(filter.destinationCountryIsoCode()));

        return flightRepository.findAll(spec, page).getContent();
    }

    @Transactional
    @Override
    public Flight sellFlight(Long flightId, int passengerDni, String passengerName, String passengerSurname) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(FlightNotFoundException::new);

        Passenger passenger = passengerService.save(new Passenger(passengerDni, passengerName, passengerSurname));
        flight.sellTicket(passenger);
        return flightRepository.save(flight);
    }
}
